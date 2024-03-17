package fr.smourad.chaos.repository;

import fr.smourad.chaos.domain.Player;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.UUID;

@Repository
public interface PlayerRepository extends ReactiveMongoRepository<Player, UUID> {

    Mono<Player> findByDiscordIdAndGuildId(BigInteger discordId, BigInteger guildId);

    Flux<Player> findAllByGuildId(BigInteger guildId);

}
