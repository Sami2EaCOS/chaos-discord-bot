package fr.smourad.chaos.repository;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import fr.smourad.chaos.domain.Player;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.UUID;

@Repository
public interface PlayerRepository extends ReactiveMongoRepository<Player, UUID> {

    Mono<Player> findByDiscordIdAndGuildId(BigInteger discordId, BigInteger guildId);

}
