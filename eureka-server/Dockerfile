# Build from root context: docker build -f eureka-server/Dockerfile .
FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle :eureka-server:build -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/eureka-server/build/libs/*.jar app.jar
EXPOSE 8761
ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["java", "-jar", "app.jar"]