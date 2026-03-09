package demo.api;

import demo.app.CreateGreetingCommand;
import demo.app.GreetingCreated;
import io.github.jokeray.mediatr4j.Mediator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/greetings")
public class GreetingController {
    private final Mediator mediator;

    public GreetingController(Mediator mediator) {
        this.mediator = mediator;
    }

    @GetMapping
    public GreetingView create(@RequestParam(name = "name", defaultValue = "world") String name) {
        GreetingCreated created = mediator.send(new CreateGreetingCommand(name))
            .toCompletableFuture()
            .join();
        return new GreetingView(created.message());
    }

    public record GreetingView(String message) {
    }
}
