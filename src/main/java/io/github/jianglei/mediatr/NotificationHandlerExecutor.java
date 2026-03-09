package io.github.jianglei.mediatr;

import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface NotificationHandlerExecutor {
    CompletionStage<Void> invoke();
}
