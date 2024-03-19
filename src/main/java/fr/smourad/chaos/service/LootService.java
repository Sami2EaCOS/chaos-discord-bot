package fr.smourad.chaos.service;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.MessageCreateSpec;
import fr.smourad.chaos.domain.Player;
import fr.smourad.chaos.domain.type.LootType;
import fr.smourad.chaos.domain.type.RoleType;
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
    private final PermissionService permissionService;
    private final Random random;
    private final GatewayDiscordClient gatewayDiscordClient;

    @Transactional
    public Mono<Void> loot(DeferrableInteractionEvent event, User user, Guild guild) {
        return playerService
                .get(user, guild)
                .flatMap(player -> loot(event, player));
    }

    @Transactional
    public Mono<Void> giveBoxes(Player player, Guild guild, int number) {
        player.setBoxes(player.getBoxes() + number);

        return gatewayDiscordClient
                .getUserById(Snowflake.of(player.getDiscordId()))
                .flatMap(user -> user.getPrivateChannel()
                        .flatMap(channel -> {
                            String text = number == 1
                                    ? "Vous avez gagné 1 nouvelle boîte sur le serveur %s"
                                    .formatted(guild.getName())
                                    : "Vous avez gagné %d nouvelles boîtes sur le serveur %s"
                                    .formatted(number, guild.getName());

                            return channel.createMessage(
                                    MessageCreateSpec.builder()
                                            .content(text)
                                            .build()
                            );
                        })
                )
                .then();
    }

    protected Mono<Void> loot(DeferrableInteractionEvent event, Player player) {
        if (player.getBoxes() <= 0) {
            return event.reply(
                    InteractionApplicationCommandCallbackSpec.builder()
                            .ephemeral(true)
                            .content("Vous n'avez plus de boîtes à ouvrir!")
                            .build()
            );
        }

        LootType loot = getRandomLoot();
        return loot(event, player, loot);
    }

    @Transactional
    public Mono<Void> loot(DeferrableInteractionEvent event, User user, Guild guild, LootType loot) {
        return playerService
                .get(user, guild)
                .flatMap(player -> loot(event, player, loot));
    }

    protected Mono<Void> loot(DeferrableInteractionEvent event, Player player, LootType loot) {
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
            case MOVE_MEMBERS -> permissionService.givePermissions(guild, player, RoleType.MOVE_MEMBERS);
            case TAG_EVERYONE -> permissionService.givePermissions(guild, player, RoleType.TAG_EVERYONE);
            case MODIFY_EMOJI -> permissionService.givePermissions(guild, player, RoleType.MODIFY_EMOJI);
            default -> Mono.empty();
        };
    }

    protected LootType getRandomLoot() {
        LootType[] loots = LootType.values();
        Integer maximum = Arrays.stream(loots)
                .map(LootType::getWeight)
                .reduce(0, Integer::sum);
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
