package io.github.jokeray.mediatr4j;

public class MediatorException extends RuntimeException {
    public MediatorException(String message) {
        super(message);
    }

    public MediatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
