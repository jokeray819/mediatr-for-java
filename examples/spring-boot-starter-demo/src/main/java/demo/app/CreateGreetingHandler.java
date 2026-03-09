package demo.app;

import io.github.jokeray.mediatr4j.Publisher;
import io.github.jokeray.mediatr4j.RequestHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component
final class CreateGreetingHandler implements RequestHandler<CreateGreetingCommand, GreetingCreated> {
    private final ObjectProvider<Publisher> publisherProvider;

    CreateGreetingHandler(ObjectProvider<Publisher> publisherProvider) {
        this.publisherProvider = publisherProvider;
    }

    @Override
    public CompletionStage<GreetingCreated> handle(CreateGreetingCommand request) {
        GreetingCreated response = new GreetingCreated("Hello, " + request.name() + "!");
        return publisherProvider.getObject()
            .publish(new GreetingCreatedNotification(request.name(), response.message()))
            .thenApply(ignored -> response);
    }
}
