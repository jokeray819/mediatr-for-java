package io.github.jokeray.mediatr4j.internal;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

public final class Futures {
    private Futures() {
    }

    public static Throwable unwrap(Throwable throwable) {
        if (throwable instanceof CompletionException completionException && completionException.getCause() != null) {
            return completionException.getCause();
        }
        return throwable;
    }

    public static <T> CompletableFuture<T> failed(Throwable throwable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);
        return future;
    }

    public static CompletionStage<Void> completedVoid() {
        return CompletableFuture.completedFuture(null);
    }
}
