package io.github.jokeray.mediatr4j;

import java.util.concurrent.CompletionStage;

public interface NotificationHandler<TNotification extends Notification> {
    CompletionStage<Void> handle(TNotification notification);
}
