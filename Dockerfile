FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/*-jar-with-dependencies.jar app.jar
COPY input.json .
CMD ["java", "-jar", "app.jar", "input.json", "output.json", "--debug", "true"]


