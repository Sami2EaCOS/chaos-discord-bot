package fr.smourad.chaos.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
@RequiredArgsConstructor
public class RandomConfig {

    @Bean
    public Random random() {
        return new Random();
    }

}
