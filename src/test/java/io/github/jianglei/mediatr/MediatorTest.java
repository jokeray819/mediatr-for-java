package io.github.jianglei.mediatr;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class MediatorTest {
    public static void main(String[] args) throws Exception {
        MediatorTest test = new MediatorTest();
        test.send_runs_pipeline_and_processors_in_order();
        test.send_void_request_dispatches_to_void_handler();
        test.publish_dispatches_to_all_notification_handlers();
        test.parallel_notification_publisher_waits_for_all_handlers();
        test.stream_request_supports_stream_pipeline();
        test.exception_actions_and_handlers_can_observe_and_recover();
        test.missing_handler_throws_meaningful_exception();
        System.out.println("Mediator self-test passed.");
    }

    void send_runs_pipeline_and_processors_in_order() {
        List<String> trace = new ArrayList<>();
        Mediator mediator = MediatorBuilder.builder()
            .registerRequestHandler(Ping.class, request -> {
                trace.add("handler");
                return CompletableFuture.completedFuture("pong:" + request.message());
            })
            .addRequestPreProcessor(Ping.class, request -> {
                trace.add("pre");
                return CompletableFuture.completedFuture(null);
            })
            .addRequestPostProcessor(Ping.class, (Ping request, String response) -> {
                trace.add("post:" + response);
                return CompletableFuture.completedFuture(null);
            })
            .addPipelineBehavior(Ping.class, (Ping request, RequestHandlerDelegate<String> next) -> {
                trace.add("before");
                return next.invoke().thenApply(response -> {
                    trace.add("after");
                    return response.toUpperCase();
                });
            })
            .build();

        String response = mediator.send(new Ping("hello")).toCompletableFuture().join();

        checkEquals("PONG:HELLO", response, "response mismatch");
        checkIterable(List.of("before", "pre", "handler", "post:pong:hello", "after"), trace, "trace mismatch");
    }

    void send_void_request_dispatches_to_void_handler() {
        AtomicInteger calls = new AtomicInteger();
        Mediator mediator = MediatorBuilder.builder()
            .registerVoidRequestHandler(Increment.class, request -> {
                calls.addAndGet(request.value());
                return CompletableFuture.completedFuture(null);
            })
            .build();

        mediator.send(new Increment(3)).toCompletableFuture().join();

        checkEquals(3, calls.get(), "void handler mismatch");
    }

    void publish_dispatches_to_all_notification_handlers() {
        List<String> trace = new ArrayList<>();
        Mediator mediator = MediatorBuilder.builder()
            .registerNotificationHandler(UserCreated.class, notification -> {
                trace.add("audit:" + notification.name());
                return CompletableFuture.completedFuture(null);
            })
            .registerNotificationHandler(UserCreated.class, notification -> {
                trace.add("email:" + notification.name());
                return CompletableFuture.completedFuture(null);
            })
            .build();

        mediator.publish(new UserCreated("alice")).toCompletableFuture().join();

        checkIterable(List.of("audit:alice", "email:alice"), trace, "notification trace mismatch");
    }

    void parallel_notification_publisher_waits_for_all_handlers() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger finished = new AtomicInteger();

        Mediator mediator = MediatorBuilder.builder()
            .notificationPublisher(new TaskWhenAllPublisher())
            .registerNotificationHandler(UserCreated.class, notification -> delayedHandler(started, release, finished))
            .registerNotificationHandler(UserCreated.class, notification -> delayedHandler(started, release, finished))
            .build();

        CompletionStage<Void> publishStage = mediator.publish(new UserCreated("bob"));
        checkTrue(started.await(1, TimeUnit.SECONDS), "parallel handlers did not start");
        release.countDown();
        publishStage.toCompletableFuture().join();

        checkEquals(2, finished.get(), "parallel publisher mismatch");
    }

    void stream_request_supports_stream_pipeline() {
        Mediator mediator = MediatorBuilder.builder()
            .registerStreamRequestHandler(CounterStream.class, request -> StreamPublishers.fromIterable(List.of(1, 2, 3)))
            .addStreamPipelineBehavior(CounterStream.class, (request, next) -> subscriber -> next.invoke().subscribe(new Flow.Subscriber<>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscriber.onSubscribe(subscription);
                }

                @Override
                public void onNext(Integer item) {
                    subscriber.onNext(item * 10);
                }

                @Override
                public void onError(Throwable throwable) {
                    subscriber.onError(throwable);
                }

                @Override
                public void onComplete() {
                    subscriber.onComplete();
                }
            }))
            .build();

        List<Integer> items = collect(mediator.createStream(new CounterStream()));

        checkIterable(List.of(10, 20, 30), items, "stream pipeline mismatch");
    }

    void exception_actions_and_handlers_can_observe_and_recover() {
        List<String> trace = new ArrayList<>();
        Mediator mediator = MediatorBuilder.builder()
            .registerRequestHandler(FailingPing.class, request -> CompletableFuture.failedFuture(new IllegalStateException("boom")))
            .addRequestExceptionAction(FailingPing.class, IllegalStateException.class, (request, exception) -> {
                trace.add("action:" + exception.getMessage());
                return CompletableFuture.completedFuture(null);
            })
            .addRequestExceptionHandler(FailingPing.class, IllegalStateException.class, (request, exception, state) -> {
                trace.add("handler");
                state.setHandled("recovered");
                return CompletableFuture.completedFuture(null);
            })
            .build();

        String response = mediator.send(new FailingPing()).toCompletableFuture().join();

        checkEquals("recovered", response, "recovery mismatch");
        checkIterable(List.of("action:boom", "handler"), trace, "exception trace mismatch");
    }

    void missing_handler_throws_meaningful_exception() {
        Mediator mediator = MediatorBuilder.builder().build();

        MediatorException exception = expectThrows(MediatorException.class, () -> mediator.send(new Ping("missing")));

        checkTrue(exception.getMessage().contains(Ping.class.getName()), "missing handler message mismatch");
    }

    private static CompletionStage<Void> delayedHandler(CountDownLatch started,
                                                        CountDownLatch release,
                                                        AtomicInteger finished) {
        started.countDown();
        return CompletableFuture.runAsync(() -> {
            try {
                release.await(1, TimeUnit.SECONDS);
                finished.incrementAndGet();
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    private static <T> List<T> collect(Flow.Publisher<T> publisher) {
        List<T> items = new ArrayList<>();
        CompletableFuture<Void> completed = new CompletableFuture<>();
        publisher.subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T item) {
                items.add(item);
            }

            @Override
            public void onError(Throwable throwable) {
                completed.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                completed.complete(null);
            }
        });
        completed.join();
        return items;
    }

    private static void checkTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void checkEquals(Object expected, Object actual, String message) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }

    private static void checkIterable(List<?> expected, List<?> actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
        }
    }

    private static <T extends Throwable> T expectThrows(Class<T> type, ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            Throwable actual = throwable instanceof java.util.concurrent.CompletionException completionException
                ? completionException.getCause()
                : throwable;
            if (type.isInstance(actual)) {
                return type.cast(actual);
            }
            throw new AssertionError("Unexpected exception type: " + actual.getClass().getName(), actual);
        }
        throw new AssertionError("Expected exception: " + type.getName());
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Throwable;
    }

    private record Ping(String message) implements Request<String> {
    }

    private record FailingPing() implements Request<String> {
    }

    private record Increment(int value) implements VoidRequest {
    }

    private record UserCreated(String name) implements Notification {
    }

    private record CounterStream() implements StreamRequest<Integer> {
    }
}
