package io.github.jokeray.mediatr4j.spring;

import io.github.jokeray.mediatr4j.BaseRequest;
import io.github.jokeray.mediatr4j.MediatorBuilder;
import io.github.jokeray.mediatr4j.Notification;
import io.github.jokeray.mediatr4j.NotificationHandler;
import io.github.jokeray.mediatr4j.PipelineBehavior;
import io.github.jokeray.mediatr4j.Request;
import io.github.jokeray.mediatr4j.RequestExceptionAction;
import io.github.jokeray.mediatr4j.RequestExceptionHandler;
import io.github.jokeray.mediatr4j.RequestHandler;
import io.github.jokeray.mediatr4j.RequestPostProcessor;
import io.github.jokeray.mediatr4j.RequestPreProcessor;
import io.github.jokeray.mediatr4j.StreamPipelineBehavior;
import io.github.jokeray.mediatr4j.StreamRequest;
import io.github.jokeray.mediatr4j.StreamRequestHandler;
import io.github.jokeray.mediatr4j.VoidRequest;
import io.github.jokeray.mediatr4j.VoidRequestHandler;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

public final class SpringMediatorRegistrar {
    private SpringMediatorRegistrar() {
    }

    public static void register(ApplicationContext applicationContext, MediatorBuilder builder) {
        applicationContext.getBeanProvider(RequestHandler.class).orderedStream()
            .forEach(handler -> registerRequestHandler(builder, handler));
        applicationContext.getBeanProvider(VoidRequestHandler.class).orderedStream()
            .forEach(handler -> registerVoidRequestHandler(builder, handler));
        applicationContext.getBeanProvider(NotificationHandler.class).orderedStream()
            .forEach(handler -> registerNotificationHandler(builder, handler));
        applicationContext.getBeanProvider(StreamRequestHandler.class).orderedStream()
            .forEach(handler -> registerStreamRequestHandler(builder, handler));
        applicationContext.getBeanProvider(PipelineBehavior.class).orderedStream()
            .forEach(behavior -> registerPipelineBehavior(builder, behavior));
        applicationContext.getBeanProvider(StreamPipelineBehavior.class).orderedStream()
            .forEach(behavior -> registerStreamPipelineBehavior(builder, behavior));
        applicationContext.getBeanProvider(RequestPreProcessor.class).orderedStream()
            .forEach(processor -> registerRequestPreProcessor(builder, processor));
        applicationContext.getBeanProvider(RequestPostProcessor.class).orderedStream()
            .forEach(processor -> registerRequestPostProcessor(builder, processor));
        applicationContext.getBeanProvider(RequestExceptionAction.class).orderedStream()
            .forEach(action -> registerExceptionAction(builder, action));
        applicationContext.getBeanProvider(RequestExceptionHandler.class).orderedStream()
            .forEach(handler -> registerExceptionHandler(builder, handler));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerRequestHandler(MediatorBuilder builder, RequestHandler<?, ?> handler) {
        Class<?> requestType = resolve(handler, RequestHandler.class, 0);
        builder.registerRequestHandler((Class<? extends Request>) requestType, (RequestHandler) handler);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerVoidRequestHandler(MediatorBuilder builder, VoidRequestHandler<?> handler) {
        Class<?> requestType = resolve(handler, VoidRequestHandler.class, 0);
        builder.registerVoidRequestHandler((Class<? extends VoidRequest>) requestType, (VoidRequestHandler) handler);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerNotificationHandler(MediatorBuilder builder, NotificationHandler<?> handler) {
        Class<?> notificationType = resolve(handler, NotificationHandler.class, 0);
        builder.registerNotificationHandler((Class<? extends Notification>) notificationType, (NotificationHandler) handler);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerStreamRequestHandler(MediatorBuilder builder, StreamRequestHandler<?, ?> handler) {
        Class<?> requestType = resolve(handler, StreamRequestHandler.class, 0);
        builder.registerStreamRequestHandler((Class<? extends StreamRequest>) requestType, (StreamRequestHandler) handler);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerPipelineBehavior(MediatorBuilder builder, PipelineBehavior<?, ?> behavior) {
        Class<?> requestType = resolve(behavior, PipelineBehavior.class, 0);
        builder.addPipelineBehavior((Class<? extends BaseRequest>) requestType, (PipelineBehavior) behavior);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerStreamPipelineBehavior(MediatorBuilder builder, StreamPipelineBehavior<?, ?> behavior) {
        Class<?> requestType = resolve(behavior, StreamPipelineBehavior.class, 0);
        builder.addStreamPipelineBehavior((Class<? extends StreamRequest>) requestType, (StreamPipelineBehavior) behavior);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerRequestPreProcessor(MediatorBuilder builder, RequestPreProcessor<?> processor) {
        Class<?> requestType = resolve(processor, RequestPreProcessor.class, 0);
        builder.addRequestPreProcessor((Class<? extends BaseRequest>) requestType, (RequestPreProcessor) processor);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerRequestPostProcessor(MediatorBuilder builder, RequestPostProcessor<?, ?> processor) {
        Class<?> requestType = resolve(processor, RequestPostProcessor.class, 0);
        builder.addRequestPostProcessor((Class<? extends BaseRequest>) requestType, (RequestPostProcessor) processor);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerExceptionAction(MediatorBuilder builder, RequestExceptionAction<?, ?> action) {
        Class<?> requestType = resolve(action, RequestExceptionAction.class, 0);
        Class<? extends Throwable> exceptionType = resolve(action, RequestExceptionAction.class, 1).asSubclass(Throwable.class);
        builder.addRequestExceptionAction((Class<? extends BaseRequest>) requestType, exceptionType, (RequestExceptionAction) action);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerExceptionHandler(MediatorBuilder builder, RequestExceptionHandler<?, ?, ?> handler) {
        Class<?> requestType = resolve(handler, RequestExceptionHandler.class, 0);
        Class<? extends Throwable> exceptionType = resolve(handler, RequestExceptionHandler.class, 2).asSubclass(Throwable.class);
        builder.addRequestExceptionHandler((Class<? extends BaseRequest>) requestType, exceptionType, (RequestExceptionHandler) handler);
    }

    private static Class<?> resolve(Object bean, Class<?> contractType, int index) {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        ResolvableType resolvableType = ResolvableType.forClass(targetClass).as(contractType);
        Class<?> resolved = resolvableType.getGeneric(index).resolve();
        if (resolved == null) {
            throw new IllegalStateException(
                "Unable to resolve generic argument " + index + " for " + contractType.getName() + " on bean " + targetClass.getName()
            );
        }
        return resolved;
    }
}
