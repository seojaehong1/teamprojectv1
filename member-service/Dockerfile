# Build from root context: docker build -f member-service/Dockerfile .
FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle :member-service:build -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/member-service/build/libs/*.jar app.jar
EXPOSE 8004
ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["java", "-jar", "app.jar"]