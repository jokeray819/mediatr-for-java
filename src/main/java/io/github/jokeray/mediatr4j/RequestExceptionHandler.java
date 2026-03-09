package io.github.jokeray.mediatr4j;

import java.util.concurrent.CompletionStage;

public interface RequestExceptionHandler<TRequest extends BaseRequest, TResponse, TException extends Throwable> {
    CompletionStage<Void> handle(TRequest request, TException exception, ExceptionHandlerState<TResponse> state);
}
