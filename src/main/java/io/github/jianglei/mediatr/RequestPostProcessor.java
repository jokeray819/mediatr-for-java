package io.github.jianglei.mediatr;

import java.util.concurrent.CompletionStage;

public interface RequestPostProcessor<TRequest extends BaseRequest, TResponse> {
    CompletionStage<Void> process(TRequest request, TResponse response);
}
