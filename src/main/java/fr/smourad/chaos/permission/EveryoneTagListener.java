package fr.smourad.chaos.permission;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import fr.smourad.chaos.event.DiscordEventHandler;
import fr.smourad.chaos.event.DiscordEventListener;
import fr.smourad.chaos.service.ActionService;
import fr.smourad.chaos.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EveryoneTagListener implements DiscordEventListener {

    private final PlayerService playerService;
    private final ActionService actionService;

    @DiscordEventHandler
    public Mono<Void> onEveryone(MessageCreateEvent event) {
        Message message = event.getMessage();
        if (!message.mentionsEveryone()) {
            return Mono.empty();
        }

        Optional<Snowflake> userId = event.getMember().map(User::getId);

        return userId
                .map(snowflake -> event.getGuild()
                        .flatMap(guild -> playerService
                                .get(snowflake, guild.getId())
                                .flatMap(player -> actionService.everyone(player, guild))
                        ))
                .orElseGet(Mono::empty);

    }

}
