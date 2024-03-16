package fr.smourad.chaos.repository;

import fr.smourad.chaos.domain.Server;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.UUID;

@Repository
public interface ServerRepository extends ReactiveMongoRepository<Server, UUID> {

    Mono<Server> findByGuildId(BigInteger guildId);

}
