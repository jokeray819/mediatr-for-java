package demo.app;

import io.github.jokeray.mediatr4j.Request;

public record CreateGreetingCommand(String name) implements Request<GreetingCreated> {
}
