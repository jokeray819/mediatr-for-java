package io.github.jokeray.mediatr4j;

import java.util.concurrent.CompletionStage;

public interface PipelineBehavior<TRequest extends BaseRequest, TResponse> {
    CompletionStage<TResponse> handle(TRequest request, RequestHandlerDelegate<TResponse> next);
}
