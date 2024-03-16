package fr.smourad.chaos.service;

import discord4j.core.object.entity.Guild;
import fr.smourad.chaos.domain.Server;
import fr.smourad.chaos.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServerService {

    private final ServerRepository repository;

    @Transactional
    public Mono<Server> get(Guild guild) {
        return repository
                .findByGuildId(guild.getId().asBigInteger())
                .switchIfEmpty(create(guild));
    }

    protected Mono<Server> create(Guild guild) {
        return repository
                .save(new Server(
                        UUID.randomUUID(),
                        guild.getId().asBigInteger()
                ));
    }

    @Transactional
    public Mono<Server> save(Server server) {
        return repository.save(server);
    }

    public Flux<Server> findAll() {
        return repository.findAll();
    }

}
