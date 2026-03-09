package io.github.jianglei.mediatr.internal;

import io.github.jianglei.mediatr.RequestExceptionAction;

public final class ExceptionActionRegistration {
    private final Class<?> requestType;
    private final Class<? extends Throwable> exceptionType;
    private final RequestExceptionAction<?, ?> action;

    public ExceptionActionRegistration(Class<?> requestType,
                                       Class<? extends Throwable> exceptionType,
                                       RequestExceptionAction<?, ?> action) {
        this.requestType = requestType;
        this.exceptionType = exceptionType;
        this.action = action;
    }

    public Class<?> requestType() {
        return requestType;
    }

    public Class<? extends Throwable> exceptionType() {
        return exceptionType;
    }

    public RequestExceptionAction<?, ?> action() {
        return action;
    }

    public boolean matches(Class<?> actualRequestType, Throwable throwable) {
        return requestType.isAssignableFrom(actualRequestType) && exceptionType.isAssignableFrom(throwable.getClass());
    }
}
