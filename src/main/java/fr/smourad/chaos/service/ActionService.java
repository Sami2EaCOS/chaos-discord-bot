package fr.smourad.chaos.service;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import discord4j.rest.util.Permission;
import fr.smourad.chaos.domain.Player;
import fr.smourad.chaos.domain.type.LootType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActionService {

    private final PlayerService playerService;
    private final PermissionService permissionService;

    @Transactional
    public Mono<Void> move(Player responsible, Player target, Guild guild) {
        return doSomething(
                responsible,
                guild,
                LootType.MOVE_MEMBERS,
                List.of(Channel.Type.GUILD_VOICE),
                List.of(Permission.MOVE_MEMBERS)
        );
    }

    @Transactional
    public Mono<Void> everyone(Player player, Guild guild) {
        return doSomething(
                player,
                guild,
                LootType.TAG_EVERYONE,
                List.of(Channel.Type.GUILD_VOICE, Channel.Type.GUILD_TEXT),
                List.of(Permission.MENTION_EVERYONE)
        );
    }

    protected Mono<Void> doSomething(Player player, Guild guild, LootType action, List<Channel.Type> types, List<Permission> permissions) {
        player.getLoots().putIfAbsent(action, 0L);
        player.getLoots().put(action, Math.max(0L, player.getLoots().get(action) - 1));

        Mono<Void> followUp = Mono.empty();
        if (player.getLoots().get(action) <= 0) {
            followUp = Flux
                    .fromIterable(types)
                    .flatMap(type -> permissionService.removePermissions(guild, player, type, permissions.toArray(new Permission[]{})))
                    .then();
        }

        return playerService.save(player).then(followUp);
    }

}
