# EBANX Take-home Assignment

## Stack

- Java 21
- Spring Boot 3
- Swagger/OpenAPI
- Docker
- In-memory storage

## Run locally

```bash
mvn clean package
java -jar target/ebanx-takehome-1.0.0.jar
```

## Swagger

http://localhost:8080/swagger-ui.html

## Docker

```bash
docker build -t ebanx-challenge .
docker run -p 8080:8080 ebanx-challenge
```