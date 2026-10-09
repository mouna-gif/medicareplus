FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline
COPY src src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
RUN useradd --system --create-home --uid 10001 medicareplus
WORKDIR /app
COPY --from=build /workspace/target/medicareplus-api-1.0.0.jar app.jar
USER medicareplus
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
