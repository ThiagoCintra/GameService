#!/usr/bin/env bash
# setup.sh — Build, start infrastructure, and run GameService end-to-end.
# Usage: ./scripts/setup.sh [--no-build] [--stop]
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# ── helpers ──────────────────────────────────────────────────────────────────
log()  { echo "[setup] $*"; }
fail() { echo "[setup] ERROR: $*" >&2; exit 1; }

# ── flags ────────────────────────────────────────────────────────────────────
NO_BUILD=false
STOP=false
for arg in "$@"; do
  case "$arg" in
    --no-build) NO_BUILD=true ;;
    --stop)     STOP=true ;;
  esac
done

# ── stop mode ────────────────────────────────────────────────────────────────
if $STOP; then
  log "Stopping infrastructure..."
  cd "$PROJECT_DIR"
  docker compose down
  exit 0
fi

# ── 1. Java 21 ───────────────────────────────────────────────────────────────
log "Checking Java..."
REQUIRED_JAVA_VERSION=21

find_java() {
  local dirs=( \
    "/usr/lib/jvm/temurin-21-jdk-amd64" \
    "/usr/lib/jvm/java-21-openjdk-amd64" \
    "/usr/lib/jvm/java-21" \
  )
  for d in "${dirs[@]}"; do
    if [[ -x "$d/bin/java" ]]; then echo "$d"; return; fi
  done
  # fallback: scan /usr/lib/jvm
  for d in /usr/lib/jvm/*/; do
    if [[ -x "$d/bin/java" ]]; then
      v=$("$d/bin/java" -version 2>&1 | awk -F '"' 'NR==1{print $2}' | cut -d. -f1)
      [[ "$v" == "$REQUIRED_JAVA_VERSION" ]] && echo "${d%/}" && return
    fi
  done
}

JAVA_HOME_DETECTED="$(find_java)"
if [[ -z "$JAVA_HOME_DETECTED" ]]; then
  fail "Java $REQUIRED_JAVA_VERSION not found. Please install it."
fi
export JAVA_HOME="$JAVA_HOME_DETECTED"
export PATH="$JAVA_HOME/bin:$PATH"
log "Using Java: $(java -version 2>&1 | head -1)"

# ── 2. Maven build ───────────────────────────────────────────────────────────
cd "$PROJECT_DIR"
if ! $NO_BUILD; then
  log "Building GameService (mvn clean package -DskipTests)..."
  mvn clean package -DskipTests -q
  log "Build complete."
fi

JAR_FILE=$(ls "$PROJECT_DIR"/target/*.jar 2>/dev/null | head -1)
[[ -z "$JAR_FILE" ]] && fail "No JAR found under target/. Run without --no-build."

# ── 3. Infrastructure via docker compose ─────────────────────────────────────
log "Starting infrastructure (localstack, mongo, redis)..."
docker compose up -d localstack mongo redis

log "Waiting for LocalStack to be healthy..."
for i in $(seq 1 30); do
  if docker compose ps localstack | grep -q "healthy"; then
    log "LocalStack is healthy."
    break
  fi
  [[ $i -eq 30 ]] && fail "LocalStack did not become healthy within 5 minutes."
  sleep 10
done

log "Waiting for MongoDB to be healthy..."
for i in $(seq 1 12); do
  if docker compose ps mongo | grep -q "healthy"; then
    log "MongoDB is healthy."
    break
  fi
  [[ $i -eq 12 ]] && fail "MongoDB did not become healthy within 2 minutes."
  sleep 10
done

# ── 4. Create SQS queues ─────────────────────────────────────────────────────
log "Creating SQS queues (idempotent)..."
bash "$SCRIPT_DIR/create_queues.sh" || log "Queue creation finished (duplicates ignored)."

# ── 5. Start GameService ─────────────────────────────────────────────────────
mkdir -p "$PROJECT_DIR/logs"
log "Starting GameService on port 8082..."
nohup java -jar "$JAR_FILE" \
  > "$PROJECT_DIR/logs/game-service.log" 2>&1 &
GAME_PID=$!
echo "$GAME_PID" > "$PROJECT_DIR/logs/game-service.pid"
log "GameService PID: $GAME_PID"

# ── 6. Health check ──────────────────────────────────────────────────────────
log "Waiting for GameService health endpoint..."
for i in $(seq 1 30); do
  if curl -sf http://localhost:8082/actuator/health > /dev/null 2>&1; then
    STATUS=$(curl -sf http://localhost:8082/actuator/health | grep -o '"status":"[^"]*"' | head -1)
    log "GameService is UP — $STATUS"
    exit 0
  fi
  sleep 5
done

log "GameService did not become healthy. Last log lines:"
tail -30 "$PROJECT_DIR/logs/game-service.log" || true
fail "GameService health check timed out."
