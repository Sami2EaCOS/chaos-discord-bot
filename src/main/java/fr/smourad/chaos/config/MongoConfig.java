package fr.smourad.chaos.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(basePackages = {"fr.smourad.chaos"})
public class MongoConfig { }


