package demo.app;

import io.github.jokeray.mediatr4j.Notification;

public record GreetingCreatedNotification(String name, String message) implements Notification {
}
