# Spring Boot Application Template/Starter-Project

[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2FSpring-Boot-Framework%2FSpring-Boot-Application-Template.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2FSpring-Boot-Framework%2FSpring-Boot-Application-Template?ref=badge_shield)

The only thing better than a Maven archetype is a repo you can fork with everything already setup. Skip the documentation and just fork-and-code.

Delete the sample code, replace with your own and you’re good to go.

## Built With

-     [Maven](https://maven.apache.org/) - Dependency Management
-     [JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) - Java™ Platform, Standard Edition Development Kit
-     [Spring Boot](https://spring.io/projects/spring-boot) - Framework to ease the bootstrapping and development of new Spring Applications
-     [H2](https://www.h2database.com/) - Very fast, open source, JDBC API
-     [git](https://git-scm.com/) - Free and Open-Source distributed version control system
-     [Prometheus](https://prometheus.io/) - Monitoring system and time series database
-     [Lombok](https://projectlombok.org/) - Never write another getter or equals method again, with one annotation your class has a fully featured builder, Automate your logging variables, and much more.
-     [Swagger](https://swagger.io/) - Open-Source software framework backed by a large ecosystem of tools that helps developers design, build, document, and consume RESTful Web services.

## External Tools Used

- [Postman](https://www.getpostman.com/) - API Development Environment (Testing Docmentation)

## To-Do

- [x] Logger (Console, JSON, LogStash)
- [x] RESTful Web Service (CRUD)
- [ ] Docker
- [x] Micrometer
- [x] Grafana

## Running the application locally

There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method in the `com.arc.sbtest.SBtemplateApplication` class from your IDE.

- Download the zip or clone the Git repository.
- Unzip the zip file (if you downloaded one)
- Open Command Prompt and Change directory (cd) to folder containing pom.xml
- Open Eclipse
  - File -> Import -> Existing Maven Project -> Navigate to the folder where you unzipped the zip
  - Select the project
- Choose the Spring Boot Application file (search for @SpringBootApplication)
- Right Click on the file and Run as Java Application

Alternatively you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:

```shell
mvn spring-boot:run
```

Before executing the project, we have to generate a new Docker Image of Spring Cloud Data Flow Server, as the official one was built with jdk-11
https://github.com/spring-cloud/spring-cloud-dataflow/issues/3507

So first build the Dockerfile:

```shell
cd docker && docker build --pull --rm -f "Dockerfile" -t springcloud/spring-cloud-dataflow-server:latest "."
```

If you want to run the Spring Cloud Data Flow example, run the following commands:

```shell
HOST_MOUNT_PATH=~/.m2/repository/ DOCKER_MOUNT_PATH=/root/.m2/repository STREAM_APPS_URI=https://dataflow.spring.io/Einstein-BUILD-SNAPSHOT-stream-applications-kafka-maven SKIPPER_VERSION=2.4.0.RC1 DATAFLOW_VERSION=2.5.0.RC1 docker-compose -f ./docker/docker-compose.yml -f ./docker/docker-compose-postgres.yml -f ./docker/docker-compose-prometheus.yml up
```

And to Shut Down the containers:

```shell
HOST_MOUNT_PATH=~/.m2/repository/ DOCKER_MOUNT_PATH=/root/.m2/repository STREAM_APPS_URI=https://dataflow.spring.io/Einstein-BUILD-SNAPSHOT-stream-applications-kafka-maven SKIPPER_VERSION=2.4.0.RC1 DATAFLOW_VERSION=2.5.0.RC1 docker-compose -f ./docker/docker-compose.yml -f ./docker/docker-compose-postgres.yml -f ./docker/docker-compose-prometheus.yml down
```

### Actuator

To monitor and manage your application

| URL                                         | Method |
| ------------------------------------------- | ------ |
| `http://localhost:8080`                     | GET    |
| `http://localhost:8080/actuator/`           | GET    |
| `http://localhost:8080/actuator/health`     | GET    |
| `http://localhost:8080/actuator/info`       | GET    |
| `http://localhost:8080/actuator/prometheus` | GET    |
| `http://localhost:8080/actuator/httptrace`  | GET    |

### URLs

| URL                                                             | Method | Remarks                 |
| --------------------------------------------------------------- | ------ | ----------------------- |
| `http://localhost:8080/bw/tech-stack`                           | GET    | Custom Response Headers |
| `http://localhost:8080/api/generic-hello`                       | GET    |                         |
| `http://localhost:8080/api/personalized-hello/`                 | GET    |                         |
| `http://localhost:8080/api/personalized-hello?name=spring-boot` | GET    |                         |
| `http://localhost:8080/api/loggers`                             | GET    |                         |

### Person URLs

| URL                                  | Method | Remarks                                                                              |
| ------------------------------------ | ------ | ------------------------------------------------------------------------------------ |
| `http://localhost:8080/api/person`   | GET    | Header `Accept:application/json` or `Accept:application/xml` for content negotiation |
| `http://localhost:8080/api/person/1` | GET    |                                                                                      |

## Documentation

- [Postman Collection](https://documenter.getpostman.com/view/2449187/RWTiwzb2) - online, with code auto-generated snippets in cURL, jQuery, Ruby,Python Requests, Node, PHP and Go programming languages
- [Postman Collection](https://github.com/fredgcosta/spring-batch-demo/blob/master/Spring%20Boot%20Template.postman_collection.json) - offline
- [Swagger](http://localhost:8088/swagger-ui.html) - Documentation & Testing

## Files and Directories

The project (a.k.a. project directory) has a particular directory structure. A representative project is shown below:

```
.
├── Spring Elements
├── src
│   └── main
│       └── java
│           ├── com.example.demo
│           ├── com.example.demo.config
│           ├── com.example.demo.controllers
│           ├── com.example.demo.exception
│           ├── com.example.demo.models
│           ├── com.example.demo.utils
│           ├── com.example.demo.repositories
│           └── com.example.demo.services
├── src
│   └── main
│       └── resources
│           ├── application.properties
│           ├── application.yml
│           ├── banner.txt
│           └── logback-spring.xml
├── src
│   └── test
│       └── java
├── JRE System Library
├── Maven Dependencies
├── target
│   └──spring-batch-demo-0.0.1-SNAPSHOT
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## packages

- `models` — to hold our entities;
- `repositories` — to communicate with the database;
- `services` — to hold our business logic;
- `security` — security configuration;
- `controllers` — to listen to the client;
- `resources/` - Contains all the static resources, templates and property files.
- `resources/application.properties` - It contains application-wide properties. Spring reads the properties defined in this file to configure your application. You can define server’s default port, server’s context path, database URLs etc, in this file.

- `test/` - contains unit and integration tests

- `pom.xml` - contains all the project dependencies

## Reporting Issues

This Project uses GitHub's integrated issue tracking system to record bugs and feature requests. If you want to raise an issue, please follow the recommendations below:

- Before you log a bug, please https://github.com/fredgcosta/spring-batch-demo/search?type=Issues[search the issue tracker]
  to see if someone has already reported the problem.
- If the issue doesn't already exist, https://github.com/fredgcosta/spring-batch-demo/issues/new[create a new issue].
- Please provide as much information as possible with the issue report.
- If you need to paste code, or include a stack trace use Markdown +++```+++ escapes before and after your text.

## Resources

- [My API Lifecycle Checklist and Scorecard](https://dzone.com/articles/my-api-lifecycle-checklist-and-scorecard)
- [HTTP Status Codes](https://www.restapitutorial.com/httpstatuscodes.html)
- [Bootstrap w3schools](https://www.w3schools.com/bootstrap/)
- [Common application properties](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html)

## License
