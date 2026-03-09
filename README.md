# mediatr4j

[中文 README](./README.zh-CN.md)

`mediatr4j` is a Java implementation of the core ideas in [.NET MediatR](https://github.com/LuckyPennySoftware/MediatR).

## Implemented capabilities

- Request/response dispatch
- Void request dispatch
- Notification publish/subscribe
- Stream request dispatch with `Flow.Publisher`
- Request pipeline behaviors
- Stream pipeline behaviors
- Request pre-processors
- Request post-processors
- Request exception actions
- Request exception handlers
- Pluggable notification publishing strategies
- Spring Boot auto-configuration and component registration

## Example

```java
Mediator mediator = MediatorBuilder.builder()
    .registerRequestHandler(CreateOrder.class, request ->
        CompletableFuture.completedFuture(new OrderCreated(request.id())))
    .addPipelineBehavior(CreateOrder.class, (request, next) -> {
        System.out.println("before");
        return next.invoke().thenApply(response -> {
            System.out.println("after");
            return response;
        });
    })
    .build();

OrderCreated created = mediator.send(new CreateOrder("A-1"))
    .toCompletableFuture()
    .join();
```

## Spring Boot

Add `@EnableMediatr4j` to your application or configuration class. Any Spring beans implementing the supported contracts will be scanned and auto-registered into the shared `Mediator` bean.

```java
@SpringBootApplication
@EnableMediatr4j(basePackageClasses = DemoApplication.class)
public class DemoApplication {
}

@Component
final class CreateOrderHandler implements RequestHandler<CreateOrder, OrderCreated> {
    @Override
    public CompletionStage<OrderCreated> handle(CreateOrder request) {
        return CompletableFuture.completedFuture(new OrderCreated(request.id()));
    }
}
```

`Mediator`, `Sender`, and `Publisher` are exposed as beans automatically. You can override the notification strategy with either a custom `NotificationPublisher` bean or configuration:

```properties
mediatr4j.notification-publisher=TASK_WHEN_ALL
```

A complete sample application is available at [examples/spring-boot-starter-demo/README.md](/Users/jianglei/my/java/Java-Mediatr/examples/spring-boot-starter-demo/README.md).
The sample uses deferred `Publisher` resolution inside the handler to avoid a Spring bean creation cycle.

## Design notes

- `.NET` MediatR integrates with DI containers through open generic registrations. This Java version uses an explicit builder/registry because Java erasure makes that pattern less natural without a container-specific adapter layer.
- Async request APIs use `CompletionStage`.
- Stream requests use `Flow.Publisher` to approximate `.NET` async streams.
- Notification publishers ship with sequential (`ForeachAwaitPublisher`) and parallel (`TaskWhenAllPublisher`) implementations.
