package io.github.jokeray.mediatr4j;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

public interface Sender {
    <TResponse> CompletionStage<TResponse> send(Request<TResponse> request);

    CompletionStage<Void> send(VoidRequest request);

    <TResponse> Flow.Publisher<TResponse> createStream(StreamRequest<TResponse> request);
}
