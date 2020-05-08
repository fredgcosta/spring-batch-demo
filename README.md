# Spring Batch example project running on Spring Cloud Data Flow

[![GitHub license](https://img.shields.io/github/license/fredgcosta/spring-batch-demo)](https://github.com/fredgcosta/spring-batch-demo/blob/master/LICENSE)

The only thing better than a Maven archetype is a repo you can fork with everything already setup. Skip the documentation and just fork-and-code.

Delete the sample code, replace with your own and you’re good to go.

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management
* [Spring Boot](https://start.spring.io/) - Spring Boot Initializer
* [OpenJDK](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot) - Java™ Platform, Standard Edition Development Kit
* [Spring Boot](https://spring.io/projects/spring-boot) - Framework to ease the bootstrapping and development of new Spring Applications
* [PostgreSQL](https://www.postgresql.org/) - The World's Most Advanced Open Source Relational Database
* [git](https://git-scm.com/) - Free and Open-Source distributed version control system
* [Prometheus](https://prometheus.io/) - Monitoring system and time series database
* [Lombok](https://projectlombok.org/) - Never write another getter or equals method again, with one annotation your class has a fully featured builder, Automate your logging variables, and much more.

## External Tools Used

* [Postman](https://www.getpostman.com/) - API Development Environment (Testing Docmentation)

## To-Do

- [x] Logger (Console, Json, ELK)
- [x] RESTful Web Service (API for Job executing)
- [ ] Docker
- [x] Spring Cloud Data Flow
- [ ] NoSQL (Redis)
- [ ] PostgreSQL (Connect to Multiple Schemas)
- [x] Micrometer
- [x] Grafna

## Running the application locally

There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method in the `com.example.demo.SpringBatchDemoApplication` class from your IDE.

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

Debug Mode
```shell
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"  
```

If you want to run the Spring Cloud Data Flow example, run the following commands:

```shell
HOST_MOUNT_PATH=~/.m2/repository/ DOCKER_MOUNT_PATH=/root/.m2/repository STREAM_APPS_URI=https://dataflow.spring.io/Einstein-BUILD-SNAPSHOT-stream-applications-kafka-maven SKIPPER_VERSION=2.4.0.RELEASE DATAFLOW_VERSION=2.5.0.RELEASE docker-compose -f ./docker/docker-compose.yml -f ./docker/docker-compose-postgres.yml -f ./docker/docker-compose-prometheus.yml up
```

And to Shut Down the containers:

```shell
HOST_MOUNT_PATH=~/.m2/repository/ DOCKER_MOUNT_PATH=/root/.m2/repository STREAM_APPS_URI=https://dataflow.spring.io/Einstein-BUILD-SNAPSHOT-stream-applications-kafka-maven SKIPPER_VERSION=2.4.0.RELEASE DATAFLOW_VERSION=2.5.0.RELEASE docker-compose -f ./docker/docker-compose.yml -f ./docker/docker-compose-postgres.yml -f ./docker/docker-compose-prometheus.yml down
```

### Tools

To monitor and manage your application

| Tool          | URL                                       | Method |
| ------------- | ----------------------------------------- | ------ |
| SCDF Dashboad | `http://localhost:9393/dashboard`         | GET    |
| Prometheus    | `http://localhost:9090/graph`             | GET    |
| Grafana       | `http://localhost:3000`                   | GET    |
| RSocket Proxy | `http://localhost:9096/metrics/connected` | GET    |
| pgAdmin4      | `http://localhost:80`                     | GET    |

### URLs

| URL | Method | Remarks |
| --- | ------ | ------- |
|     | GET    |         |
|     | GET    |         |
|     | GET    |         |
|     | GET    |         |
|     | GET    |         |

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
│           ├── com.example.demo.models
│           ├── com.example.demo.repositories
│           ├── com.example.demo.steps
│           ├── com.example.demo.steps.chunklets
│           ├── com.example.demo.steps.mappers
│           ├── com.example.demo.steps.tasklets
│           └── com.example.demo.steps.tokenizers
├── src
│   └── main
│       └── resources
            ├── input
            │   └── exemplo-sou-java.txt 
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
