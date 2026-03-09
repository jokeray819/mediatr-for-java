package io.github.jianglei.mediatr;

import java.util.concurrent.CompletionStage;

public interface VoidRequestHandler<TRequest extends VoidRequest> {
    CompletionStage<Void> handle(TRequest request);
}
