# Introduction
In the [previous article](("https://medium.com/@hiromaki58/use-aws-spot-instances-without-interruptions-1-set-up-the-development-environment-using-docker-5bc8e6e3b551" 1, Set up the development environment using Docker.)), we set up a development environment using Docker.
As part of the environment setup, I would like to build the CI portion of the CI/CD pipeline again — specifically, running tests on GitHub push using CircleCI.
# What This Article Covers
- Modifying the application-test.properties file for Spring Boot
- Connecting CircleCI with GitHub
- Creating the .circleci/config.yml file
#### Overall Workflow
1, Connect CircleCI with GitHub
2, Modify the application-test.properties file for Spring Boot
3, Create the .circleci/config.yml file
4, Add necessary configurations to build.gradle
5, Add test code
6, Conclusion
#### Environment
- Backend: Spring Boot
- Databases: MySQL on CircleCI
- CI Tool: CircleCI
- Build Tool: Gradle
# 1, Connect CircleCI with GitHub
First, go to the CircleCI dashboard and click on “Projects” from the left-hand menu.
Once your list of GitHub repositories is displayed, click “Set Up Project” next to the repository you want to integrate.
Select the language and build configuration template, then add and push the .circleci/config.yml file to your repository.
# 2, Modify the application-test.properties file for Spring Boot
The config file contains sensitive information such as passwords.
These values are specified using CircleCI environment variables.
To keep this article concise, I’ll also cover that setup in a separate post.

Below is the content of the config file.
```properties:application-test.properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/${DB_NAME}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```
# 3, Create the .circleci/config.yml file
```yml:config.yml
version: 2.1

executors:
  java-executor:
    docker:
      - image: gradle:8.13-jdk23
        environment:
          SPRING_PROFILES_ACTIVE: test
          DB_NAME: webgame
          DB_USER: $LOCAL_DB_USER
          DB_PASS: $LOCAL_DB_PASSWORD
      - image: mysql:8.0
        environment:
          MYSQL_DATABASE: webgame
          MYSQL_ROOT_PASSWORD: $LOCAL_DB_PASSWORD
        command: >-
          mysqld --character-set-server=utf8mb4
                 --collation-server=utf8mb4_unicode_ci
    working_directory: ~/project

jobs:
  test-java:
    executor: java-executor
    steps:
      - checkout:
          path: ~/project

      - run:
          name: Wait for MySQL
          command: |
            for i in `seq 1 20`; do
              mysql -h 127.0.0.1 -P 3306 -u root -e "SELECT 1" && break
              echo "Waiting for MySQL..."
              sleep 3
            done

      - run:
          name: Make gradlew executable
          command: chmod +x ./web_game/gradlew

      - run:
          name: Run tests
          command: |
            ./web_game/gradlew test \
              --project-dir ./web_game \
              -i \

      - store_test_results:
          path: web_game/build/test-results

      - store_artifacts:
          path: web_game/build/reports

workflows:
  build-and-test:
    jobs:
      - test-java
```
# 4, Add necessary configurations to build.gradle
```gradle:build.gradle
dependencies {
  // Spring Boot Dependencies
  implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
  implementation 'org.springframework.boot:spring-boot-starter-web'
  implementation 'org.springframework.boot:spring-boot-starter-mail'

  // Testing Dependencies
  testImplementation 'org.springframework.boot:spring-boot-starter-test'
  testImplementation 'com.h2database:h2'
}
```
# 5, Add test code
```java:PlayerControllerTest.java
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class PlayerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PlayerRepository playerRepository;
    @MockitoBean
    private JavaMailSender javaMailSender;

    @BeforeEach
    public void setUp() {
        playerRepository.deleteAll();

        PlayerModel player = new PlayerModel();
        player.setFirstName("John");
        player.setFamilyName("Doe");
        player.setEmailAddr("test@example.com");
        player.setPassword("password123");
        playerRepository.save(player);
    }

    @Test
    public void testLoginSuccess() throws Exception {
        mockMvc.perform(post("/api/player/login")
                .with(csrf())
                .content("{\"emailAddr\": \"test@example.com\", \"password\": \"password123\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loginTry").value(true))
                .andExpect(jsonPath("$.msg").value("Succeed to login"));
    }
}
```
# 6, Conclusion
Each time you push to the remote repository, the tests will be executed.