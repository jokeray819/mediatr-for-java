package io.github.jokeray.mediatr4j.spring;

import io.github.jokeray.mediatr4j.ForeachAwaitPublisher;
import io.github.jokeray.mediatr4j.Mediator;
import io.github.jokeray.mediatr4j.MediatorBuilder;
import io.github.jokeray.mediatr4j.NotificationPublisher;
import io.github.jokeray.mediatr4j.Publisher;
import io.github.jokeray.mediatr4j.Sender;
import io.github.jokeray.mediatr4j.TaskWhenAllPublisher;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(Mediatr4jProperties.class)
public class Mediatr4jAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(NotificationPublisher.class)
    public NotificationPublisher mediatr4jNotificationPublisher(Mediatr4jProperties properties) {
        if (properties.getNotificationPublisher() == NotificationPublisherMode.TASK_WHEN_ALL) {
            return new TaskWhenAllPublisher();
        }
        return new ForeachAwaitPublisher();
    }

    @Bean
    @ConditionalOnMissingBean(Mediator.class)
    public Mediator mediator(ApplicationContext applicationContext,
                             NotificationPublisher notificationPublisher,
                             ObjectProvider<Mediatr4jConfigurer> configurers) {
        MediatorBuilder builder = MediatorBuilder.builder().notificationPublisher(notificationPublisher);
        SpringMediatorRegistrar.register(applicationContext, builder);
        configurers.orderedStream().forEach(configurer -> configurer.configure(builder));
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean(Sender.class)
    public Sender sender(Mediator mediator) {
        return mediator;
    }

    @Bean
    @ConditionalOnMissingBean(Publisher.class)
    public Publisher publisher(Mediator mediator) {
        return mediator;
    }
}
