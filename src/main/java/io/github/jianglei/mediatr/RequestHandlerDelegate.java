package io.github.jianglei.mediatr;

import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface RequestHandlerDelegate<TResponse> {
    CompletionStage<TResponse> invoke();
}
