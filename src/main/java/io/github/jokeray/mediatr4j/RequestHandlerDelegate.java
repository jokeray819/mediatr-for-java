package io.github.jokeray.mediatr4j;

import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface RequestHandlerDelegate<TResponse> {
    CompletionStage<TResponse> invoke();
}
