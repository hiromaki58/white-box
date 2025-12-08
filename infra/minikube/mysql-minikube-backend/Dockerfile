# =====================================
# 1) Build Stage (Gradle + JDK11)
# =====================================
FROM gradle:7.6-jdk11 AS build

WORKDIR /home/app

COPY build.gradle settings.gradle ./
COPY src ./src

# Build Spring Boot JAR
RUN gradle clean build -x test --no-daemon


# =====================================
# 2) Runtime Stage (JRE11 slim)
# =====================================
FROM eclipse-temurin:11-jre

WORKDIR /app

# Copy the JAR from build stage
COPY --from=build /home/app/build/libs/*.jar app.jar

# Expose backend port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
