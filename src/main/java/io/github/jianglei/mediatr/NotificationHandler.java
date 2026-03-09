package io.github.jianglei.mediatr;

import java.util.concurrent.CompletionStage;

public interface NotificationHandler<TNotification extends Notification> {
    CompletionStage<Void> handle(TNotification notification);
}
