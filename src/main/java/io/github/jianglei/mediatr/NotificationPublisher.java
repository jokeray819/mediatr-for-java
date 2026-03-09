package io.github.jianglei.mediatr;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface NotificationPublisher {
    CompletionStage<Void> publish(Notification notification, List<NotificationHandlerExecutor> handlers);
}
