package fr.smourad.chaos.service;

import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import fr.smourad.chaos.domain.Player;
import fr.smourad.chaos.domain.type.LootType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Random;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LootService {

    private final PlayerService playerService;
    private final Random random;

    @Transactional
    public Mono<Void> loot(DeferrableInteractionEvent event, User user, Guild guild) {
        return playerService
                .get(user, guild)
                .flatMap(player -> loot(event, player));
    }

    public Mono<Void> loot(DeferrableInteractionEvent event, Player player) {
        if (player.getBoxes() == 0) {
            player.setBoxes(1);
            return event.reply("rien");
        }

        LootType loot = getRandomLoot();
        player.getLoots().put(loot, player.getLoots().get(loot) + 1);
        player.setBoxes(player.getBoxes() - 1);

        return playerService
                .save(player)
                .then(event.reply(loot.toString()));
    }

    protected LootType getRandomLoot() {
        LootType[] loots = LootType.values();
        Integer maximum = Arrays.stream(loots).map(LootType::getWeight).reduce(0, Integer::sum);
        int weight = random.nextInt(maximum);

        for (LootType loot : loots) {
            if (loot.getWeight() > weight) {
                return loot;
            } else {
                weight -= loot.getWeight();
            }
        }

        throw new RuntimeException("No loot found");
    }

}
