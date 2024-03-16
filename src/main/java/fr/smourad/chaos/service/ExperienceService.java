package fr.smourad.chaos.service;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
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
    private final LootService lootService;

    @Transactional
    public Mono<Player> add(Player player, long experience) {
        BigInteger total = player.getExperience()
                .add(BigInteger.valueOf(experience));
        player.setExperience(total);
        return playerService.save(player);
    }

    public Mono<Void> check(User user, Guild guild, BigInteger before, BigInteger after) {
        BigInteger experienceForOneLevel = BigInteger.valueOf(10_000);
        BigInteger levelBefore = before.mod(experienceForOneLevel);
        BigInteger levelAfter = after.mod(experienceForOneLevel);

        if (levelBefore.compareTo(levelAfter) < 0)  {
            return lootService.giveBoxes(user, guild, 1);
        }

        return Mono.empty();
    }

}
