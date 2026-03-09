package demo;

import io.github.jokeray.mediatr4j.spring.EnableMediatr4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableMediatr4j(basePackageClasses = DemoApplication.class)
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
