package io.github.jianglei.mediatr;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.Flow;

public final class StreamPublishers {
    private StreamPublishers() {
    }

    public static <T> Flow.Publisher<T> fromIterable(Iterable<T> items) {
        Objects.requireNonNull(items, "items");
        return subscriber -> subscriber.onSubscribe(new IterableSubscription<>(subscriber, items.iterator()));
    }

    private static final class IterableSubscription<T> implements Flow.Subscription {
        private final Flow.Subscriber<? super T> subscriber;
        private final Iterator<T> iterator;
        private boolean cancelled;

        private IterableSubscription(Flow.Subscriber<? super T> subscriber, Iterator<T> iterator) {
            this.subscriber = subscriber;
            this.iterator = iterator;
        }

        @Override
        public void request(long n) {
            if (cancelled) {
                return;
            }
            if (n <= 0) {
                cancelled = true;
                subscriber.onError(new IllegalArgumentException("Demand must be positive."));
                return;
            }
            long remaining = n;
            try {
                while (!cancelled && remaining-- > 0 && iterator.hasNext()) {
                    subscriber.onNext(iterator.next());
                }
                if (!cancelled && !iterator.hasNext()) {
                    cancelled = true;
                    subscriber.onComplete();
                }
            } catch (Throwable throwable) {
                cancelled = true;
                subscriber.onError(throwable);
            }
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }
}
