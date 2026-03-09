package io.github.jokeray.mediatr4j;

import java.util.concurrent.Flow;

@FunctionalInterface
public interface StreamHandlerDelegate<TResponse> {
    Flow.Publisher<TResponse> invoke();
}
