package fr.smourad.chaos.service;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import fr.smourad.chaos.domain.Player;
import fr.smourad.chaos.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository repository;

    @Transactional
    public Mono<Player> get(User user, Guild guild) {
        return repository
                .findByDiscordIdAndGuildId(
                        user.getId().asBigInteger(),
                        guild.getId().asBigInteger()
                )
                .switchIfEmpty(create(user, guild));
    }

    protected Mono<Player> create(User user, Guild guild) {
        return repository
                .save(new Player(
                        UUID.randomUUID(),
                        user.getId().asBigInteger(),
                        guild.getId().asBigInteger()
                ));
    }

    @Transactional
    public Mono<Player> save(Player player) {
        return repository.save(player);
    }

}
