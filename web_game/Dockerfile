# 1. Base image to build Java application using Maven
FROM gradle:8.13-jdk23 AS build

# Set the working directory in the Docker image
WORKDIR /home/app

# Copy application source code into the working directory in the Docker image
COPY src /home/app/src
COPY build.gradle /home/app
COPY settings.gradle /home/app

# Use Gradle to build the project
RUN gradle clean build --no-daemon

# 2. Base image to run the Java JAR file
FROM fedora:latest

# 必要なパッケージをインストール
RUN dnf update -y
RUN dnf install -y java-latest-openjdk-devel
RUN sudo dnf install -y openssh
RUN sudo dnf install -y openssh-clients

# Set environment variable PATH
# ENV PATH="${PATH}:/usr/bin/ssh"
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /home/app/build/libs/*.jar /usr/local/lib/web-game.jar

# Specify the port the application uses
EXPOSE 8080

# Java アプリケーションを実行
ENTRYPOINT ["java", "-jar", "/app/web-game.jar"]