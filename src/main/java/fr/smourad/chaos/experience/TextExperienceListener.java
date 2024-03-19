package fr.smourad.chaos.experience;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import fr.smourad.chaos.event.DiscordEventHandler;
import fr.smourad.chaos.event.DiscordEventListener;
import fr.smourad.chaos.service.ExperienceService;
import fr.smourad.chaos.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TextExperienceListener implements DiscordEventListener {

    private final PlayerService playerService;
    private final ExperienceService experienceService;
    private final GatewayDiscordClient gatewayDiscordClient;

    @DiscordEventHandler
    @Transactional
    public Mono<Void> onText(MessageCreateEvent event) {
        Optional<Member> member = event.getMember();
        Instant now = Instant.now();

        return member
                .filter(m -> !m.isBot())
                .map(m -> event
                        .getGuild()
                        .flatMap(guild -> playerService
                                .get(m.getId(), guild.getId())
                                .flatMap(player -> {
                                    Instant lastMessageDate = player.getLastMessageDate();

                                    if (Objects.nonNull(lastMessageDate) && lastMessageDate.isAfter(now.minus(60, ChronoUnit.SECONDS))) {
                                        return Mono.empty();
                                    }

                                    return event
                                            .getMessage()
                                            .getChannel()
                                            .flatMap(channel -> channel
                                                    .getMessagesBefore(event.getMessage().getId())
                                                    .next()
                                                    .flatMap(Message::getAuthorAsMember)
                                                    .filter(author -> !author.isBot())
                                                    .flatMap(author -> gatewayDiscordClient
                                                            .getUserById(Snowflake.of(player.getDiscordId()))
                                                            .flatMap(user -> {
                                                                if (author.getId().asBigInteger().compareTo(player.getDiscordId()) != 0) {
                                                                    BigInteger experience = player.getExperience();
                                                                    BigInteger result = experience.add(BigInteger.valueOf(200));
                                                                    player.setExperience(result);
                                                                    player.setLastMessageDate(now);

                                                                    return experienceService.check(player, guild, experience, result);
                                                                }

                                                                return Mono.empty();
                                                            })
                                                            .thenReturn(player)
                                                    ));
                                })
                                .flatMap(playerService::save)
                                .then()
                        )
                )
                .orElseGet(Mono::empty);
    }

}
