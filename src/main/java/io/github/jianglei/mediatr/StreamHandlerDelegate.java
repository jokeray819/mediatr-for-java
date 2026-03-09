package io.github.jianglei.mediatr;

import java.util.concurrent.Flow;

@FunctionalInterface
public interface StreamHandlerDelegate<TResponse> {
    Flow.Publisher<TResponse> invoke();
}
