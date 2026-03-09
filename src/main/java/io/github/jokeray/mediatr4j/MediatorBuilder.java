package io.github.jokeray.mediatr4j;

import io.github.jokeray.mediatr4j.internal.ExceptionActionRegistration;
import io.github.jokeray.mediatr4j.internal.ExceptionHandlerRegistration;
import io.github.jokeray.mediatr4j.internal.Registration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MediatorBuilder {
    private final Map<Class<?>, RequestHandler<?, ?>> requestHandlers = new LinkedHashMap<>();
    private final Map<Class<?>, VoidRequestHandler<?>> voidRequestHandlers = new LinkedHashMap<>();
    private final Map<Class<?>, StreamRequestHandler<?, ?>> streamHandlers = new LinkedHashMap<>();
    private final List<Registration<NotificationHandler<?>>> notificationHandlers = new ArrayList<>();
    private final List<Registration<PipelineBehavior<?, ?>>> pipelineBehaviors = new ArrayList<>();
    private final List<Registration<StreamPipelineBehavior<?, ?>>> streamPipelineBehaviors = new ArrayList<>();
    private final List<Registration<RequestPreProcessor<?>>> requestPreProcessors = new ArrayList<>();
    private final List<Registration<RequestPostProcessor<?, ?>>> requestPostProcessors = new ArrayList<>();
    private final List<ExceptionActionRegistration> exceptionActions = new ArrayList<>();
    private final List<ExceptionHandlerRegistration> exceptionHandlers = new ArrayList<>();
    private NotificationPublisher notificationPublisher = new ForeachAwaitPublisher();

    public static MediatorBuilder builder() {
        return new MediatorBuilder();
    }

    public <TRequest extends Request<TResponse>, TResponse> MediatorBuilder registerRequestHandler(
        Class<TRequest> requestType,
        RequestHandler<TRequest, TResponse> handler) {
        requestHandlers.put(requestType, handler);
        return this;
    }

    public <TRequest extends VoidRequest> MediatorBuilder registerVoidRequestHandler(
        Class<TRequest> requestType,
        VoidRequestHandler<TRequest> handler) {
        voidRequestHandlers.put(requestType, handler);
        return this;
    }

    public <TNotification extends Notification> MediatorBuilder registerNotificationHandler(
        Class<TNotification> notificationType,
        NotificationHandler<TNotification> handler) {
        notificationHandlers.add(new Registration<>(notificationType, handler));
        return this;
    }

    public <TRequest extends StreamRequest<TResponse>, TResponse> MediatorBuilder registerStreamRequestHandler(
        Class<TRequest> requestType,
        StreamRequestHandler<TRequest, TResponse> handler) {
        streamHandlers.put(requestType, handler);
        return this;
    }

    public <TRequest extends BaseRequest, TResponse> MediatorBuilder addPipelineBehavior(
        Class<TRequest> requestType,
        PipelineBehavior<TRequest, TResponse> behavior) {
        pipelineBehaviors.add(new Registration<>(requestType, behavior));
        return this;
    }

    public <TRequest extends StreamRequest<TResponse>, TResponse> MediatorBuilder addStreamPipelineBehavior(
        Class<TRequest> requestType,
        StreamPipelineBehavior<TRequest, TResponse> behavior) {
        streamPipelineBehaviors.add(new Registration<>(requestType, behavior));
        return this;
    }

    public <TRequest extends BaseRequest> MediatorBuilder addRequestPreProcessor(
        Class<TRequest> requestType,
        RequestPreProcessor<TRequest> processor) {
        requestPreProcessors.add(new Registration<>(requestType, processor));
        return this;
    }

    public <TRequest extends BaseRequest, TResponse> MediatorBuilder addRequestPostProcessor(
        Class<TRequest> requestType,
        RequestPostProcessor<TRequest, TResponse> processor) {
        requestPostProcessors.add(new Registration<>(requestType, processor));
        return this;
    }

    public <TRequest extends BaseRequest, TResponse, TException extends Throwable> MediatorBuilder addRequestExceptionHandler(
        Class<TRequest> requestType,
        Class<TException> exceptionType,
        RequestExceptionHandler<TRequest, TResponse, TException> handler) {
        exceptionHandlers.add(new ExceptionHandlerRegistration(requestType, exceptionType, handler));
        return this;
    }

    public <TRequest extends BaseRequest, TException extends Throwable> MediatorBuilder addRequestExceptionAction(
        Class<TRequest> requestType,
        Class<TException> exceptionType,
        RequestExceptionAction<TRequest, TException> action) {
        exceptionActions.add(new ExceptionActionRegistration(requestType, exceptionType, action));
        return this;
    }

    public MediatorBuilder notificationPublisher(NotificationPublisher notificationPublisher) {
        this.notificationPublisher = notificationPublisher;
        return this;
    }

    public Mediator build() {
        return new DefaultMediator(
            Map.copyOf(requestHandlers),
            Map.copyOf(voidRequestHandlers),
            Map.copyOf(streamHandlers),
            List.copyOf(notificationHandlers),
            List.copyOf(pipelineBehaviors),
            List.copyOf(streamPipelineBehaviors),
            List.copyOf(requestPreProcessors),
            List.copyOf(requestPostProcessors),
            List.copyOf(exceptionActions),
            List.copyOf(exceptionHandlers),
            notificationPublisher
        );
    }
}
