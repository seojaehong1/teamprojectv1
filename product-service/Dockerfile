# Build from root context: docker build -f product-service/Dockerfile .
FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle :product-service:build -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/product-service/build/libs/*.jar app.jar
EXPOSE 8001
ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["java", "-jar", "app.jar"]