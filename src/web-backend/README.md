# Web backend application

Web backend API implementation. Implemented with Java and Spring Boot.

## How to run

(You need to have Java 17 and Maven installed.)

First build the application and run the tests:
```mvn clean install```

Then run the application:
```mvn -f ./api/pom.xml spring-boot:run```

The API will be available at [http://localhost:8080/api](http://localhost:8080/api).

## Swagger documentation

API documentation is available
at [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)
or [http://localhost:8080/api/swagger-ui/index.html](http://localhost:8080/api/swagger-ui/index.html).
(You can even authorize in the Swagger UI)

The OpenAPI specification is available
at [http://localhost:8080/api/v3/api-docs](http://localhost:8080/api/v3/api-docs).

## Project structure

- **api**: API layer module
- **bl**: Business logic layer module
- **dal**: Data access layer module
- **shared**: Shared module
- *pom.xml*: Maven configuration
- *README.md*: This file
