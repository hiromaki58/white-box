# Introduction
In the [previous article](("https://medium.com/p/5bc8e6e3b551" 1, Set up the development environment using Docker.)), we set up a development environment using Docker.
As a continuation of the environment setup, this time we will build a CI/CD pipeline using CircleCI.
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
- Databases: AWS RDS for production, H2 (in-memory) for CI tests
- CI Tool: CircleCI
- Build Tool: Gradle
# 1, Connect CircleCI with GitHub
First, go to the CircleCI dashboard and click on “Projects” from the left-hand menu.
Once your list of GitHub repositories is displayed, click “Set Up Project” next to the repository you want to integrate.
Select the language and build configuration template, then add and push the .circleci/config.yml file to your repository.
# 2, Modify the application-test.properties file for Spring Boot
```properties:application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
```
# 3, Create the .circleci/config.yml file
```yml:config.yml
version: 2.1

executors:
  java-executor:
    docker:
      - image: gradle:8.13-jdk23
    working_directory: ~/project

jobs:
  test-java:
    executor: java-executor
    steps:
      - checkout:
          path: ~/project

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
        playerRepository.deleteAll(); // clear the existing data

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