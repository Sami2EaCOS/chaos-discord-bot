package fr.smourad.chaos.service;

import discord4j.core.object.entity.Guild;
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

    private final LootService lootService;

    @Transactional
    public Mono<Void> check(Player player, Guild guild, BigInteger before, BigInteger after) {
        BigInteger experienceForOneLevel = BigInteger.valueOf(10_000);
        BigInteger levelBefore = before.divide(experienceForOneLevel);
        BigInteger levelAfter = after.divide(experienceForOneLevel);

        if (levelBefore.compareTo(levelAfter) < 0)  {
            return lootService.giveBoxes(player, guild, 1);
        }

        return Mono.empty();
    }

}
