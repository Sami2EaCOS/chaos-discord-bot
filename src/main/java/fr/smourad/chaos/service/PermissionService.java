package fr.smourad.chaos.service;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import fr.smourad.chaos.domain.Player;
import fr.smourad.chaos.domain.type.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionService {

    private final ServerService serverService;

    public Mono<Void> removePermissions(Guild guild, Player player, RoleType roleType) {
        return guild
                .getMemberById(Snowflake.of(player.getDiscordId()))
                .zipWith(serverService.get(guild))
                .flatMap(tuple -> tuple.getT1()
                        .removeRole(Snowflake.of(tuple.getT2().getRoles().get(roleType)))
                )
                .then();
    }

    public Mono<Void> givePermissions(Guild guild, Player player, RoleType roleType) {
        return guild
                .getMemberById(Snowflake.of(player.getDiscordId()))
                .zipWith(serverService.get(guild))
                .flatMap(tuple -> tuple.getT1()
                        .addRole(Snowflake.of(tuple.getT2().getRoles().get(roleType)))
                )
                .then();
    }

}
