package io.github.jianglei.mediatr;

import java.util.concurrent.CompletionStage;

public interface RequestExceptionHandler<TRequest extends BaseRequest, TResponse, TException extends Throwable> {
    CompletionStage<Void> handle(TRequest request, TException exception, ExceptionHandlerState<TResponse> state);
}
