package io.github.jokeray.mediatr4j;

import java.util.concurrent.CompletionStage;

public interface VoidRequestHandler<TRequest extends VoidRequest> {
    CompletionStage<Void> handle(TRequest request);
}
