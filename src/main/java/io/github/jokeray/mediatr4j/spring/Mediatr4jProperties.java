package io.github.jokeray.mediatr4j.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mediatr4j")
public class Mediatr4jProperties {
    private NotificationPublisherMode notificationPublisher = NotificationPublisherMode.FOREACH_AWAIT;

    public NotificationPublisherMode getNotificationPublisher() {
        return notificationPublisher;
    }

    public void setNotificationPublisher(NotificationPublisherMode notificationPublisher) {
        this.notificationPublisher = notificationPublisher;
    }
}
