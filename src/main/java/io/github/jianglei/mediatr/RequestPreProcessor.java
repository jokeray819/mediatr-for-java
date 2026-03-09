package io.github.jianglei.mediatr;

import java.util.concurrent.CompletionStage;

public interface RequestPreProcessor<TRequest extends BaseRequest> {
    CompletionStage<Void> process(TRequest request);
}
