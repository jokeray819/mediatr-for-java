package io.github.jokeray.mediatr4j;

import io.github.jokeray.mediatr4j.internal.ExceptionActionRegistration;
import io.github.jokeray.mediatr4j.internal.ExceptionHandlerRegistration;
import io.github.jokeray.mediatr4j.internal.Futures;
import io.github.jokeray.mediatr4j.internal.Registration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

final class DefaultMediator implements Mediator {
    private final Map<Class<?>, RequestHandler<?, ?>> requestHandlers;
    private final Map<Class<?>, VoidRequestHandler<?>> voidRequestHandlers;
    private final Map<Class<?>, StreamRequestHandler<?, ?>> streamHandlers;
    private final List<Registration<NotificationHandler<?>>> notificationHandlers;
    private final List<Registration<PipelineBehavior<?, ?>>> pipelineBehaviors;
    private final List<Registration<StreamPipelineBehavior<?, ?>>> streamPipelineBehaviors;
    private final List<Registration<RequestPreProcessor<?>>> requestPreProcessors;
    private final List<Registration<RequestPostProcessor<?, ?>>> requestPostProcessors;
    private final List<ExceptionActionRegistration> exceptionActions;
    private final List<ExceptionHandlerRegistration> exceptionHandlers;
    private final NotificationPublisher notificationPublisher;

    DefaultMediator(Map<Class<?>, RequestHandler<?, ?>> requestHandlers,
                    Map<Class<?>, VoidRequestHandler<?>> voidRequestHandlers,
                    Map<Class<?>, StreamRequestHandler<?, ?>> streamHandlers,
                    List<Registration<NotificationHandler<?>>> notificationHandlers,
                    List<Registration<PipelineBehavior<?, ?>>> pipelineBehaviors,
                    List<Registration<StreamPipelineBehavior<?, ?>>> streamPipelineBehaviors,
                    List<Registration<RequestPreProcessor<?>>> requestPreProcessors,
                    List<Registration<RequestPostProcessor<?, ?>>> requestPostProcessors,
                    List<ExceptionActionRegistration> exceptionActions,
                    List<ExceptionHandlerRegistration> exceptionHandlers,
                    NotificationPublisher notificationPublisher) {
        this.requestHandlers = requestHandlers;
        this.voidRequestHandlers = voidRequestHandlers;
        this.streamHandlers = streamHandlers;
        this.notificationHandlers = notificationHandlers;
        this.pipelineBehaviors = pipelineBehaviors;
        this.streamPipelineBehaviors = streamPipelineBehaviors;
        this.requestPreProcessors = requestPreProcessors;
        this.requestPostProcessors = requestPostProcessors;
        this.exceptionActions = exceptionActions;
        this.exceptionHandlers = exceptionHandlers;
        this.notificationPublisher = notificationPublisher;
    }

    @Override
    public <TResponse> CompletionStage<TResponse> send(Request<TResponse> request) {
        Class<?> requestType = request.getClass();
        RequestHandler<Request<TResponse>, TResponse> handler = castRequestHandler(requestHandlers.get(requestType), requestType);
        RequestHandlerDelegate<TResponse> handlerDelegate = () -> handler.handle(request);
        return buildRequestPipeline(request, handlerDelegate).invoke();
    }

    @Override
    public CompletionStage<Void> send(VoidRequest request) {
        Class<?> requestType = request.getClass();
        VoidRequestHandler<VoidRequest> handler = castVoidRequestHandler(voidRequestHandlers.get(requestType), requestType);
        RequestHandlerDelegate<Void> handlerDelegate = () -> handler.handle(request);
        return buildRequestPipeline(request, handlerDelegate).invoke();
    }

    @Override
    public <TResponse> Flow.Publisher<TResponse> createStream(StreamRequest<TResponse> request) {
        Class<?> requestType = request.getClass();
        StreamRequestHandler<StreamRequest<TResponse>, TResponse> handler =
            castStreamHandler(streamHandlers.get(requestType), requestType);
        StreamHandlerDelegate<TResponse> delegate = () -> handler.handle(request);

        List<Registration<StreamPipelineBehavior<?, ?>>> matches = streamPipelineBehaviors.stream()
            .filter(registration -> registration.matches(requestType))
            .toList();

        for (int i = matches.size() - 1; i >= 0; i--) {
            StreamPipelineBehavior<StreamRequest<TResponse>, TResponse> behavior = castStreamBehavior(matches.get(i).handler());
            StreamHandlerDelegate<TResponse> next = delegate;
            delegate = () -> behavior.handle(request, next);
        }

        return delegate.invoke();
    }

    @Override
    public CompletionStage<Void> publish(Notification notification) {
        Class<?> notificationType = notification.getClass();
        List<NotificationHandlerExecutor> handlers = new java.util.ArrayList<>();
        for (Registration<NotificationHandler<?>> registration : notificationHandlers) {
            if (!registration.matches(notificationType)) {
                continue;
            }
            handlers.add(() -> castNotificationHandler(registration.handler()).handle(notification));
        }
        return notificationPublisher.publish(notification, handlers);
    }

    private <TRequest extends BaseRequest, TResponse> RequestHandlerDelegate<TResponse> buildRequestPipeline(
        TRequest request,
        RequestHandlerDelegate<TResponse> handlerDelegate) {
        Class<?> requestType = request.getClass();

        RequestHandlerDelegate<TResponse> delegate = () -> runPreProcessors(request)
            .thenCompose(ignored -> handlerDelegate.invoke())
            .thenCompose(response -> runPostProcessors(request, response).thenApply(ignored -> response))
            .handle((response, throwable) -> handleRequestOutcome(request, response, throwable))
            .thenCompose(stage -> stage);

        List<Registration<PipelineBehavior<?, ?>>> matches = pipelineBehaviors.stream()
            .filter(registration -> registration.matches(requestType))
            .toList();

        for (int i = matches.size() - 1; i >= 0; i--) {
            PipelineBehavior<TRequest, TResponse> behavior = castPipelineBehavior(matches.get(i).handler());
            RequestHandlerDelegate<TResponse> next = delegate;
            delegate = () -> behavior.handle(request, next);
        }

        return delegate;
    }

    private <TRequest extends BaseRequest> CompletionStage<Void> runPreProcessors(TRequest request) {
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (Registration<RequestPreProcessor<?>> registration : requestPreProcessors) {
            if (!registration.matches(request.getClass())) {
                continue;
            }
            RequestPreProcessor<TRequest> processor = castPreProcessor(registration.handler());
            chain = chain.thenCompose(ignored -> processor.process(request).toCompletableFuture());
        }
        return chain;
    }

    private <TRequest extends BaseRequest, TResponse> CompletionStage<Void> runPostProcessors(TRequest request, TResponse response) {
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (Registration<RequestPostProcessor<?, ?>> registration : requestPostProcessors) {
            if (!registration.matches(request.getClass())) {
                continue;
            }
            RequestPostProcessor<TRequest, TResponse> processor = castPostProcessor(registration.handler());
            chain = chain.thenCompose(ignored -> processor.process(request, response).toCompletableFuture());
        }
        return chain;
    }

    private <TRequest extends BaseRequest, TResponse> CompletionStage<TResponse> handleRequestOutcome(
        TRequest request,
        TResponse response,
        Throwable throwable) {
        if (throwable == null) {
            return CompletableFuture.completedFuture(response);
        }

        Throwable actual = Futures.unwrap(throwable);
        CompletionStage<Void> actions = runExceptionActions(request, actual);
        return actions
            .thenCompose(ignored -> this.<TRequest, TResponse>tryHandleException(request, actual))
            .thenCompose((ExceptionHandlerState<TResponse> state) -> {
                if (state.isHandled()) {
                    return CompletableFuture.<TResponse>completedFuture(state.response());
                }
                return Futures.<TResponse>failed(actual);
            });
    }

    private <TRequest extends BaseRequest> CompletionStage<Void> runExceptionActions(TRequest request, Throwable throwable) {
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (ExceptionActionRegistration registration : exceptionActions) {
            if (!registration.matches(request.getClass(), throwable)) {
                continue;
            }
            RequestExceptionAction<TRequest, Throwable> action = castExceptionAction(registration.action());
            chain = chain.thenCompose(ignored -> action.execute(request, throwable).toCompletableFuture());
        }
        return chain;
    }

    private <TRequest extends BaseRequest, TResponse> CompletionStage<ExceptionHandlerState<TResponse>> tryHandleException(
        TRequest request,
        Throwable throwable) {
        ExceptionHandlerState<TResponse> state = new ExceptionHandlerState<>();
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (ExceptionHandlerRegistration registration : exceptionHandlers) {
            if (!registration.matches(request.getClass(), throwable)) {
                continue;
            }
            RequestExceptionHandler<TRequest, TResponse, Throwable> handler = castExceptionHandler(registration.handler());
            chain = chain.thenCompose(ignored -> {
                if (state.isHandled()) {
                    return Futures.completedVoid();
                }
                return handler.handle(request, throwable, state);
            }).toCompletableFuture();
        }
        return chain.thenApply(ignored -> state);
    }

    @SuppressWarnings("unchecked")
    private static <TResponse> RequestHandler<Request<TResponse>, TResponse> castRequestHandler(Object handler, Class<?> requestType) {
        if (handler == null) {
            throw new MediatorException("No request handler registered for " + requestType.getName());
        }
        return (RequestHandler<Request<TResponse>, TResponse>) handler;
    }

    @SuppressWarnings("unchecked")
    private static VoidRequestHandler<VoidRequest> castVoidRequestHandler(Object handler, Class<?> requestType) {
        if (handler == null) {
            throw new MediatorException("No void request handler registered for " + requestType.getName());
        }
        return (VoidRequestHandler<VoidRequest>) handler;
    }

    @SuppressWarnings("unchecked")
    private static <TResponse> StreamRequestHandler<StreamRequest<TResponse>, TResponse> castStreamHandler(Object handler,
                                                                                                            Class<?> requestType) {
        if (handler == null) {
            throw new MediatorException("No stream request handler registered for " + requestType.getName());
        }
        return (StreamRequestHandler<StreamRequest<TResponse>, TResponse>) handler;
    }

    @SuppressWarnings("unchecked")
    private static NotificationHandler<Notification> castNotificationHandler(Object handler) {
        return (NotificationHandler<Notification>) handler;
    }

    @SuppressWarnings("unchecked")
    private static <TRequest extends BaseRequest, TResponse> PipelineBehavior<TRequest, TResponse> castPipelineBehavior(Object behavior) {
        return (PipelineBehavior<TRequest, TResponse>) behavior;
    }

    @SuppressWarnings("unchecked")
    private static <TRequest extends StreamRequest<TResponse>, TResponse> StreamPipelineBehavior<TRequest, TResponse> castStreamBehavior(
        Object behavior) {
        return (StreamPipelineBehavior<TRequest, TResponse>) behavior;
    }

    @SuppressWarnings("unchecked")
    private static <TRequest extends BaseRequest> RequestPreProcessor<TRequest> castPreProcessor(Object processor) {
        return (RequestPreProcessor<TRequest>) processor;
    }

    @SuppressWarnings("unchecked")
    private static <TRequest extends BaseRequest, TResponse> RequestPostProcessor<TRequest, TResponse> castPostProcessor(Object processor) {
        return (RequestPostProcessor<TRequest, TResponse>) processor;
    }

    @SuppressWarnings("unchecked")
    private static <TRequest extends BaseRequest> RequestExceptionAction<TRequest, Throwable> castExceptionAction(Object action) {
        return (RequestExceptionAction<TRequest, Throwable>) action;
    }

    @SuppressWarnings("unchecked")
    private static <TRequest extends BaseRequest, TResponse> RequestExceptionHandler<TRequest, TResponse, Throwable> castExceptionHandler(
        Object handler) {
        return (RequestExceptionHandler<TRequest, TResponse, Throwable>) handler;
    }
}
