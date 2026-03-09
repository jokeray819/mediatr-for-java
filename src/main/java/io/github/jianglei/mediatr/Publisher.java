package io.github.jianglei.mediatr;

import java.util.concurrent.CompletionStage;

public interface Publisher {
    CompletionStage<Void> publish(Notification notification);
}
