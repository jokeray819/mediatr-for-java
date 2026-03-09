package demo.app;

import io.github.jokeray.mediatr4j.RequestPreProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component
@Order(-100)
final class GreetingValidationProcessor implements RequestPreProcessor<CreateGreetingCommand> {
    @Override
    public CompletionStage<Void> process(CreateGreetingCommand request) {
        if (request.name() == null || request.name().isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("name must not be blank"));
        }
        return CompletableFuture.completedFuture(null);
    }
}
