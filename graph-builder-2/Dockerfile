FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY ../datalake-builder/pom.xml .
COPY ../datalake-builder/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
