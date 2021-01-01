package cz.dsw.distribguide.example01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Example01Application implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(Example01Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Example01Application.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
    }
/*
	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			System.out.println("Let's inspect the beans provided by Spring Boot:");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				System.out.println(beanName);
			}
		};

	}
*/
}
