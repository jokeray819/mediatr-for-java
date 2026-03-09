package io.github.jokeray.mediatr4j;

import java.util.concurrent.CompletionStage;

public interface RequestPreProcessor<TRequest extends BaseRequest> {
    CompletionStage<Void> process(TRequest request);
}
