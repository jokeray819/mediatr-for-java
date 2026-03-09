package io.github.jokeray.mediatr4j;

import java.util.concurrent.Flow;

public interface StreamPipelineBehavior<TRequest extends StreamRequest<TResponse>, TResponse> {
    Flow.Publisher<TResponse> handle(TRequest request, StreamHandlerDelegate<TResponse> next);
}
