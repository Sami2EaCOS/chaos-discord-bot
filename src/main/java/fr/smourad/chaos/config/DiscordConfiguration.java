package fr.smourad.chaos.config;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.shard.GatewayBootstrap;
import discord4j.gateway.GatewayOptions;
import discord4j.gateway.intent.IntentSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;

@Configuration
public class DiscordConfiguration {

    @Value("${token}")
    private String token;

    @Bean
    public DiscordClient discordClient() {
        return DiscordClientBuilder.create(token).build();
    }

    @Bean
    public EventDispatcher eventDispatcher() {
        return EventDispatcher.builder().build();
    }

    @Bean
    @DependsOn({"discordEventLoader"})
    public GatewayDiscordClient gatewayDiscordClient(
            DiscordClient discordClient,
            EventDispatcher eventDispatcher,
            ReactiveMongoOperations reactiveMongoOperations
    ) {
        GatewayBootstrap<GatewayOptions> gateway = discordClient.gateway();
        return gateway
                .setEventDispatcher(eventDispatcher)
                .setEnabledIntents(IntentSet.all())
                .login()
                .block();
    }

}
