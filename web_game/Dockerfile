# 1. Base image to build Java application using Maven
FROM maven:3.8.4-openjdk-23 AS build

# Copy application source code into the working directory in the Docker image
COPY src /home/app/src
COPY pom.xml /home/app

# Use Maven to build the project
RUN mvn -f /home/app/pom.xml clean package

# 2. Base image to run the Java JAR file
FROM openjdk:23

# Copy the built JAR file from the build stage
COPY --from=build /home/app/target/aaa-bbb-0.0.1-SNAPSHOT.jar /usr/local/lib/aaa-bbb.jar

# Specify the port the application uses
EXPOSE 8080

# Command to run the application when the container starts
ENTRYPOINT ["java","-jar","/usr/local/lib/aaa-bbb.jar"]
