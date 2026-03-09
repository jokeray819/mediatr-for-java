package io.github.jokeray.mediatr4j.spring;

import io.github.jokeray.mediatr4j.MediatorBuilder;

@FunctionalInterface
public interface Mediatr4jConfigurer {
    void configure(MediatorBuilder builder);
}
