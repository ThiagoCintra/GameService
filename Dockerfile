## multi-stage build: build with maven then run on lightweight JRE
FROM maven:3.10.1-eclipse-temurin-21 AS build
WORKDIR /workspace/app
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src src
RUN mvn -DskipTests package -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/app/target/game-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-XX:+UseSerialGC","-jar","/app.jar"]
