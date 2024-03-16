package fr.smourad.chaos.experience;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.VoiceChannel;
import fr.smourad.chaos.domain.Player;
import fr.smourad.chaos.service.ExperienceService;
import fr.smourad.chaos.service.PlayerService;
import fr.smourad.chaos.service.ServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class VocalExperienceScheduler {

    private final ServerService serverService;
    private final PlayerService playerService;
    private final ExperienceService experienceService;
    private final GatewayDiscordClient gatewayDiscordClient;

    @Scheduled(fixedDelay = 10_000)
    public void updateVocalExperience() {
        serverService.findAll()
                .flatMap(server -> gatewayDiscordClient
                        .getGuildById(Snowflake.of(server.getGuildId()))
                        .flatMap(guild -> guild.getChannels()
                                .filter(channel -> Objects.equals(channel.getType(), Channel.Type.GUILD_VOICE))
                                .ofType(VoiceChannel.class)
                                .flatMap(channel -> channel.getVoiceStates()
                                        .flatMap(VoiceState::getMember)
                                        .flatMap(member -> playerService.get(member.getId(), guild.getId()))
                                        .collectList()
                                )
                                .flatMap(players -> giveExperience(guild, players))
                                .then()
                        ))
                .subscribe();
    }

    protected Mono<Void> giveExperience(Guild guild, List<Player> players) {
        int others = players.size() - 1;
        int gain = (int) IntStream
                .range(0, others)
                .mapToDouble(i -> 1.2)
                .reduce(10, (a, b) -> a * b);

        return Flux.fromIterable(players)
                .flatMap(player -> {
                    BigInteger experience = player.getExperience();
                    BigInteger result = experience.add(BigInteger.valueOf(gain));

                    player.setExperience(result);

                    return gatewayDiscordClient
                            .getUserById(Snowflake.of(player.getDiscordId()))
                            .flatMap(user -> experienceService.check(user, guild, experience, result))
                            .thenReturn(player);
                })
                .flatMap(playerService::save)
                .then();
    }


}
