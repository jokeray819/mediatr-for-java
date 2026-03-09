package demo.app;

import io.github.jokeray.mediatr4j.PipelineBehavior;
import io.github.jokeray.mediatr4j.RequestHandlerDelegate;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletionStage;

@Component
@Order(0)
final class GreetingLoggingBehavior implements PipelineBehavior<CreateGreetingCommand, GreetingCreated> {
    @Override
    public CompletionStage<GreetingCreated> handle(CreateGreetingCommand request, RequestHandlerDelegate<GreetingCreated> next) {
        System.out.println("handling request for: " + request.name());
        return next.invoke().thenApply(response -> {
            System.out.println("handled response: " + response.message());
            return response;
        });
    }
}
