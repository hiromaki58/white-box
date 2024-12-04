# white-box

This repository has 5 packages as the portfolio.

1. [linux_updating]
2. [web_game]
3. [web_game_interface]
4. [react_native_expo]
5. [typeprof-steep]

## 1 liux_updating

### Purpose

To update the linux node, establish the SSH connection first, send the commands secondly and output logs.

### Technology used

- Excecutable file: jar
- Back-end: Java 8
- Frame work: Spring
- Server: Apach Tomcat

### Installation

Excute the jar file with the CSV file path.
Command: java -jar XXX.jar src/path/to/CSV/file/YYY.csv

## 2 web_game(In progress)

### Purpose

This repo demonstrates the implementation of a portfolio game interface using Spring Boot for the backend. The project showcases how to integrate these technologies to create a web-based game application with user authentication, game score management, and a responsive frontend.

### Features

- **Backend:**
  - Built with **Spring Boot**.
  - Provides RESTful APIs for frontend communication.
  - Includes **Spring Security** for authentication and authorization.
  - Manages user data and game scores using a relational database.

- **Database:**
  - Stores user credentials and game scores.
  - Uses  **MySQL**.

- **Integration:**
  - CORS enabled for seamless integration between React and Spring Boot during development.
  - The deployment environment is set up in **AWS**:
    - **EC2 Instance:** Hosts the Spring Boot application, assigned with a public IP address for external access.
    - **RDS Instance:** MySQL database is deployed in a private subnet with only a private IP address.
    - **Route 53:** Configured to use an SSL certificate for secure communication.
    - Note: Since this is a portfolio project, advanced services like Load Balancers or CloudFront are not used.


## 3 web_game_interface(In progress)

### Purpose

This repo is for a web application GUI built using React, TypeScript and CSS, designed to showcase and play games. Below is a detailed summary of the application structure, features, and development setup.

### Features

- **Game List**: A collection of games displayed in a card layout on the index page.
- **Reusable Components**: Shared `Header` and `Footer` components across all pages.
- **Dynamic Pages**:
  - Index page displaying game cards.
  - Individual game pages displaying the selected game.
- **Login Functionality**:
  - Optional login for users.
  - Displays the highest scores for logged-in users (fetched from MySQL).
  - Allows users to set a profile picture and view it in the header.
- **Backend Integration**:
  - Java Spring Boot for authentication and database management.
  - REST APIs for user data and game scores.

## 4 react_native_expo

### Purpose

To test "expo" frame work and create the basic react native app.

### Technology used

- Frame work: Expo
- Front-end: React native

### Installation

Excecute the code with the command.
Command: expo start

## 5 TypeProf and Steep Testing

### Purpose

Make the funciton to check the data type with RBS, TypeProf and Steep
