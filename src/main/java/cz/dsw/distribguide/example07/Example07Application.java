package cz.dsw.distribguide.example07;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Example07Application implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(Example07Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Example07Application.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
    }
}
