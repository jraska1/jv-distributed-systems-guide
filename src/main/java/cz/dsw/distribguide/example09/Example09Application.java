package cz.dsw.distribguide.example09;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Example09Application implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(Example09Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Example09Application.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
    }
}
