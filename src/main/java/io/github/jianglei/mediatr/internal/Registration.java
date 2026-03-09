package io.github.jianglei.mediatr.internal;

public final class Registration<T> {
    private final Class<?> requestType;
    private final T handler;

    public Registration(Class<?> requestType, T handler) {
        this.requestType = requestType;
        this.handler = handler;
    }

    public Class<?> requestType() {
        return requestType;
    }

    public T handler() {
        return handler;
    }

    public boolean matches(Class<?> actualType) {
        return requestType.isAssignableFrom(actualType);
    }
}
