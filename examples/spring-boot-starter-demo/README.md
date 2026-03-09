# mediatr4j Spring Boot Starter Demo

This example shows how to use `mediatr4j` with Spring Boot auto-configuration and `@EnableMediatr4j`.

## What it contains

- A REST endpoint at `GET /greetings?name=Alice`
- A `RequestHandler` for request/response dispatch
- A `NotificationHandler` triggered after the request completes
- A `PipelineBehavior` for request logging
- A `RequestPreProcessor` for request validation
- Deferred `Publisher` lookup to avoid mediator/handler circular dependency during bean creation

## Run

Install `mediatr4j` to your local Maven repository first, then run the demo:

```bash
mvn install
cd examples/spring-boot-starter-demo
mvn spring-boot:run
```

## Try it

```bash
curl "http://localhost:8080/greetings?name=Alice"
```

Expected response:

```json
{"message":"Hello, Alice!"}
```

You should also see log lines from the pipeline behavior and notification handler in the console.

If you change controller method signatures, keep explicit request parameter names such as `@RequestParam(name = "name")`, or compile with `-parameters`.
