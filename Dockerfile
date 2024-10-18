FROM gradle:8.10.2-jdk21 AS build

WORKDIR /app

COPY . .

RUN gradle clean build

FROM openjdk:21-jdk-slim

WORKDIR /spring-boot

COPY --from=build /app/build/libs/*SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/spring-boot/app.jar"]