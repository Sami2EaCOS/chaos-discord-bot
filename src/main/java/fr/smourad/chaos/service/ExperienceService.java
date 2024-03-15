package fr.smourad.chaos.service;

import fr.smourad.chaos.domain.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExperienceService {

    private final PlayerService playerService;

    @Transactional
    public Mono<Player> add(Player player, long experience) {
        BigInteger total = player.getExperience().add(BigInteger.valueOf(experience));
        player.setExperience(total);
        return playerService.save(player);
    }

}
