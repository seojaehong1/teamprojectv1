# Build from root context: docker build -f inventory-service/Dockerfile .
FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle :inventory-service:build -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/inventory-service/build/libs/*.jar app.jar
EXPOSE 8008
ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["java", "-jar", "app.jar"]