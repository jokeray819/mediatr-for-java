package demo.app;

import io.github.jokeray.mediatr4j.NotificationHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component
final class GreetingCreatedNotificationHandler implements NotificationHandler<GreetingCreatedNotification> {
    @Override
    public CompletionStage<Void> handle(GreetingCreatedNotification notification) {
        System.out.println("notification received: " + notification.message());
        return CompletableFuture.completedFuture(null);
    }
}
