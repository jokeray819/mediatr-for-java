package io.github.jokeray.mediatr4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class TaskWhenAllPublisher implements NotificationPublisher {
    @Override
    public CompletionStage<Void> publish(Notification notification, List<NotificationHandlerExecutor> handlers) {
        CompletableFuture<?>[] tasks = handlers.stream()
            .map(handler -> handler.invoke().toCompletableFuture())
            .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(tasks);
    }
}
