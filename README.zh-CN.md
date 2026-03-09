# mediatr4j

[English README](./README.md)

`mediatr4j` 是一个基于 Java 实现的中介者模式库，参考了 [.NET MediatR](https://github.com/LuckyPennySoftware/MediatR) 的核心设计。

## 已实现功能

- 请求/响应派发
- 无返回值请求派发
- 通知发布/订阅
- 基于 `Flow.Publisher` 的流式请求
- 请求管道行为 `PipelineBehavior`
- 流式管道行为 `StreamPipelineBehavior`
- 请求前置处理器
- 请求后置处理器
- 请求异常动作
- 请求异常处理器
- 可插拔的通知发布策略
- Spring Boot 自动配置与组件自动注册

## Maven 坐标

```xml
<dependency>
    <groupId>io.github.jokeray</groupId>
    <artifactId>mediatr4j</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## 基础示例

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

## Spring Boot 集成

在应用类或配置类上添加 `@EnableMediatr4j`。实现了支持接口的 Spring Bean 会被自动扫描，并注册到共享的 `Mediator` Bean 中。

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

框架会自动暴露 `Mediator`、`Sender`、`Publisher` 三个 Bean。你也可以通过自定义 `NotificationPublisher` Bean，或者直接使用配置项切换通知分发策略：

```properties
mediatr4j.notification-publisher=TASK_WHEN_ALL
```

可选值：

- `FOREACH_AWAIT`：顺序执行通知处理器
- `TASK_WHEN_ALL`：并行等待所有通知处理器完成

完整示例应用见 [examples/spring-boot-starter-demo/README.md](/Users/jianglei/my/java/Java-Mediatr/examples/spring-boot-starter-demo/README.md)。
示例里在 handler 内部延迟获取 `Publisher`，用于避免 Spring 创建 `Mediator` 与 handler 时出现循环依赖。

## 设计说明

- `.NET` MediatR 主要依赖 DI 容器中的开放泛型注册。Java 版本没有直接复制这套机制，而是使用显式 `MediatorBuilder` 注册，更符合 Java 泛型擦除后的使用习惯。
- 异步请求接口统一使用 `CompletionStage`。
- 流式请求使用 `Flow.Publisher` 对应 `.NET` 的异步流模型。
- 内置两种通知发布器：顺序执行的 `ForeachAwaitPublisher` 和并行执行的 `TaskWhenAllPublisher`。

## 当前限制

- 当前项目已提供 Spring Boot 自动配置，但在没有 Spring 依赖的纯 Java 环境中，这部分能力不会生效。
- 当前仓库内的验证以本地自检为主；如果需要完整的 Spring 集成测试，还需要在可联网或已缓存依赖的环境中运行 Maven/Gradle。

## 发版到 Maven Central

仓库已加入通过 Sonatype Central Portal 发布到 Maven Central 的 GitHub Actions 流程，并使用 `central-publishing-maven-plugin`。

你需要在 GitHub 仓库中配置以下 secrets：

- `MAVEN_CENTRAL_USERNAME`
- `MAVEN_CENTRAL_PASSWORD`
- `MAVEN_GPG_PRIVATE_KEY`
- `MAVEN_GPG_PASSPHRASE`

工作流文件见 [.github/workflows/release.yml](/Users/jianglei/my/java/Java-Mediatr/.github/workflows/release.yml)。当 GitHub Release 被发布时，流程会自动执行。
