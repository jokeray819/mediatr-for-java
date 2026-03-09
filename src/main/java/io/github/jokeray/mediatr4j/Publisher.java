package io.github.jokeray.mediatr4j;

import java.util.concurrent.CompletionStage;

public interface Publisher {
    CompletionStage<Void> publish(Notification notification);
}
