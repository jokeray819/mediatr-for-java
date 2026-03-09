package io.github.jianglei.mediatr;

import java.util.concurrent.CompletionStage;

public interface RequestHandler<TRequest extends Request<TResponse>, TResponse> {
    CompletionStage<TResponse> handle(TRequest request);
}
