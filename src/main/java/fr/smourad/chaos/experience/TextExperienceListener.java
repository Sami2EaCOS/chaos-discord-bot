package fr.smourad.chaos.experience;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import fr.smourad.chaos.event.DiscordEventHandler;
import fr.smourad.chaos.event.DiscordEventListener;
import fr.smourad.chaos.service.ExperienceService;
import fr.smourad.chaos.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TextExperienceListener implements DiscordEventListener {

    private final PlayerService playerService;
    private final ExperienceService experienceService;
    private final GatewayDiscordClient gatewayDiscordClient;

    @DiscordEventHandler
    public Mono<Void> onText(MessageCreateEvent event) {
        Optional<Member> member = event.getMember();

        return member
                .map(m -> event.getGuild()
                        .flatMap(guild -> playerService
                                .get(m.getId(), guild.getId())
                                .flatMap(player -> {
                                    Instant lastMessageDate = player.getLastMessageDate();
                                    Instant now = Instant.now();

                                    if (Objects.nonNull(lastMessageDate) && lastMessageDate.isAfter(now.minus(60, ChronoUnit.SECONDS))) {
                                        return Mono.empty();
                                    }

                                    player.setLastMessageDate(now);

                                    BigInteger experience = player.getExperience();
                                    BigInteger result = experience.add(BigInteger.TEN);
                                    player.setExperience(result);

                                    return gatewayDiscordClient
                                            .getUserById(Snowflake.of(player.getDiscordId()))
                                            .flatMap(user -> experienceService.check(user, guild, experience, result))
                                            .thenReturn(player);
                                })
                                .flatMap(playerService::save)
                                .then()
                        )
                )
                .orElseGet(Mono::empty);
    }

}
