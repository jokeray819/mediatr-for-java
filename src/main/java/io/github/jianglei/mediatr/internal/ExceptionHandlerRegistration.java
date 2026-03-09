package io.github.jianglei.mediatr.internal;

import io.github.jianglei.mediatr.RequestExceptionHandler;

public final class ExceptionHandlerRegistration {
    private final Class<?> requestType;
    private final Class<? extends Throwable> exceptionType;
    private final RequestExceptionHandler<?, ?, ?> handler;

    public ExceptionHandlerRegistration(Class<?> requestType,
                                        Class<? extends Throwable> exceptionType,
                                        RequestExceptionHandler<?, ?, ?> handler) {
        this.requestType = requestType;
        this.exceptionType = exceptionType;
        this.handler = handler;
    }

    public Class<?> requestType() {
        return requestType;
    }

    public Class<? extends Throwable> exceptionType() {
        return exceptionType;
    }

    public RequestExceptionHandler<?, ?, ?> handler() {
        return handler;
    }

    public boolean matches(Class<?> actualRequestType, Throwable throwable) {
        return requestType.isAssignableFrom(actualRequestType) && exceptionType.isAssignableFrom(throwable.getClass());
    }
}
