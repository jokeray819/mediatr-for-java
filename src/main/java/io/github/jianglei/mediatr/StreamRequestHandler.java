package io.github.jianglei.mediatr;

import java.util.concurrent.Flow;

public interface StreamRequestHandler<TRequest extends StreamRequest<TResponse>, TResponse> {
    Flow.Publisher<TResponse> handle(TRequest request);
}
