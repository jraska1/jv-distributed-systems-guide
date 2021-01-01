package cz.dsw.distribguide.example08;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class Example08Application implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(Example08Application.class);

    private static final String[] NODES = {"applicant01", "provider01", "provider02", "provider03"};

    public static void main(String[] args) {
        SpringApplication.run(Example08Application.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
    }

    @Bean
    public Map<String, KeyPair> keys() {
        Map<String, KeyPair> m = new HashMap<>();
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);

            for (String name : NODES) {
                m.put(name, kpg.generateKeyPair());
            }
        } catch (NoSuchAlgorithmException e) {
            logger.error("Security exception", e);
        }
        logger.info("Key Pairs Initialized ...");
        return m;
    }
}
