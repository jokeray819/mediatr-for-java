package io.github.jianglei.mediatr;

import java.util.concurrent.CompletionStage;

public interface RequestExceptionAction<TRequest extends BaseRequest, TException extends Throwable> {
    CompletionStage<Void> execute(TRequest request, TException exception);
}
