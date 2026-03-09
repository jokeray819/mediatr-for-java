# mediatr4j

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

## Design notes

- `.NET` MediatR integrates with DI containers through open generic registrations. This Java version uses an explicit builder/registry because Java erasure makes that pattern less natural without a container-specific adapter layer.
- Async request APIs use `CompletionStage`.
- Stream requests use `Flow.Publisher` to approximate `.NET` async streams.
- Notification publishers ship with sequential (`ForeachAwaitPublisher`) and parallel (`TaskWhenAllPublisher`) implementations.
