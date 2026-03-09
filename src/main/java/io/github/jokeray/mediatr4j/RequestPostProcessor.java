package io.github.jokeray.mediatr4j;

import java.util.concurrent.CompletionStage;

public interface RequestPostProcessor<TRequest extends BaseRequest, TResponse> {
    CompletionStage<Void> process(TRequest request, TResponse response);
}
