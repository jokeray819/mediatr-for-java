package io.github.jianglei.mediatr;

public class MediatorException extends RuntimeException {
    public MediatorException(String message) {
        super(message);
    }

    public MediatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
