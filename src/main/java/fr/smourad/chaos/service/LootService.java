package fr.smourad.chaos.service;

import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.rest.util.Permission;
import fr.smourad.chaos.domain.Player;
import fr.smourad.chaos.domain.type.LootType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LootService {

    private final PlayerService playerService;
    private final PermissionService permissionService;
    private final Random random;

    @Transactional
    public Mono<Void> loot(DeferrableInteractionEvent event, User user, Guild guild) {
        return playerService
                .get(user, guild)
                .flatMap(player -> loot(event, player));
    }

    public Mono<Void> loot(DeferrableInteractionEvent event, Player player) {
        if (player.getBoxes() <= 0) {
            player.setBoxes(1);
            return playerService.save(player).then(event.reply("rien"));
        }

        LootType loot = getRandomLoot();
        player.getLoots().putIfAbsent(loot, 0L);
        player.getLoots().put(loot, player.getLoots().get(loot) + 1L);
        player.setBoxes(player.getBoxes() - 1);

        return playerService
                .save(player)
                .then(event.getInteraction().getGuild()
                        .flatMap(guild -> addPermissions(guild, player, loot))
                )
                .then(event.reply(loot.toString()));
    }

    protected Mono<Void> addPermissions(Guild guild, Player player, LootType loot) {
        return switch (loot) {
            case MOVE_MEMBERS -> permissionService.givePermissions(
                    guild, player, Channel.Type.GUILD_VOICE, Permission.MOVE_MEMBERS
            );
            case TAG_EVERYONE -> Flux
                    .fromIterable(List.of(Channel.Type.GUILD_VOICE, Channel.Type.GUILD_TEXT))
                    .flatMap(type -> permissionService.givePermissions(
                            guild, player, type, Permission.MENTION_EVERYONE
                    ))
                    .then();
            default -> Mono.empty();
        };
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
