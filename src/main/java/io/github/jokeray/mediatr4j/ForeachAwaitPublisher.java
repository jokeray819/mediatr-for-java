package io.github.jokeray.mediatr4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class ForeachAwaitPublisher implements NotificationPublisher {
    @Override
    public CompletionStage<Void> publish(Notification notification, List<NotificationHandlerExecutor> handlers) {
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (NotificationHandlerExecutor handler : handlers) {
            chain = chain.thenCompose(ignored -> handler.invoke().toCompletableFuture());
        }
        return chain;
    }
}
