# -------- BUILD --------
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src

RUN mvn -B -q clean package -DskipTests

# -------- RUNTIME --------
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# evita problema de versão de jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java","-jar","/app/app.jar"]