package io.github.jokeray.mediatr4j;

import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface NotificationHandlerExecutor {
    CompletionStage<Void> invoke();
}
