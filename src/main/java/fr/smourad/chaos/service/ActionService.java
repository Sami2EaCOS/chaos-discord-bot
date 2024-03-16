package fr.smourad.chaos.service;

import discord4j.core.object.entity.Guild;
import fr.smourad.chaos.domain.Player;
import fr.smourad.chaos.domain.type.LootType;
import fr.smourad.chaos.domain.type.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActionService {

    private final PlayerService playerService;
    private final PermissionService permissionService;

    @Transactional
    public Mono<Void> move(Player responsible, Player target, Guild guild) {
        return doSomethingWithRole(
                responsible,
                guild,
                LootType.MOVE_MEMBERS,
                RoleType.MOVE_MEMBERS
        );
    }

    @Transactional
    public Mono<Void> everyone(Player player, Guild guild) {
        return doSomethingWithRole(
                player,
                guild,
                LootType.TAG_EVERYONE,
                RoleType.TAG_EVERYONE
        );
    }

    protected Mono<Void> doSomethingWithRole(Player player, Guild guild, LootType action, RoleType roleType) {
        player.getLoots().putIfAbsent(action, 0L);
        player.getLoots().put(action, Math.max(0L, player.getLoots().get(action) - 1));

        Mono<Void> followUp = Mono.empty();
        if (player.getLoots().get(action) <= 0) {
            followUp = permissionService.removePermissions(guild, player, roleType);
        }

        return playerService.save(player).then(followUp);
    }

}
