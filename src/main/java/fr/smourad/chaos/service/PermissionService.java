package fr.smourad.chaos.service;

import discord4j.common.util.Snowflake;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.CategorizableChannel;
import discord4j.core.object.entity.channel.Channel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import fr.smourad.chaos.domain.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionService {

    public Mono<Void> removePermissions(Guild guild, Player player, Channel.Type type, Permission... permissionsToRemove) {
        return guild.getChannels()
                .filter(channel -> Objects.equals(channel.getType(), type))
                .ofType(CategorizableChannel.class)
                .flatMap(channel -> {
                    Snowflake playerId = Snowflake.of(player.getDiscordId());

                    return channel
                            .getOverwriteForMember(playerId)
                            .map(permissions -> {
                                PermissionSet allowed = permissions.getAllowed()
                                        .andNot(PermissionSet.of(permissionsToRemove));
                                PermissionSet denied = permissions.getDenied()
                                        .or(PermissionSet.of(permissionsToRemove));

                                return channel.addMemberOverwrite(
                                        playerId,
                                        PermissionOverwrite.forMember(playerId, allowed, denied)
                                );
                            }).orElseGet(() -> {
                                PermissionSet allowed = PermissionSet.none();
                                PermissionSet denied = PermissionSet.of(permissionsToRemove);

                                return channel
                                        .addMemberOverwrite(
                                                playerId,
                                                PermissionOverwrite.forMember(playerId, allowed, denied)
                                        );
                            });
                })
                .then();
    }

    public Mono<Void> givePermissions(Guild guild, Player player, Channel.Type type, Permission... permissionsToAdd) {
        return guild.getChannels()
                .filter(channel -> Objects.equals(channel.getType(), type))
                .ofType(CategorizableChannel.class)
                .flatMap(channel -> {
                    Snowflake playerId = Snowflake.of(player.getDiscordId());

                    return channel
                            .getOverwriteForMember(playerId)
                            .map(permissions -> {
                                PermissionSet allowed = permissions.getAllowed()
                                        .or(PermissionSet.of(permissionsToAdd));
                                PermissionSet denied = permissions.getDenied()
                                        .andNot(PermissionSet.of(permissionsToAdd));

                                return channel.addMemberOverwrite(
                                        playerId,
                                        PermissionOverwrite.forMember(playerId, allowed, denied)
                                );
                            })
                            .orElseGet(() -> {
                                PermissionSet allowed = PermissionSet.of(permissionsToAdd);
                                PermissionSet denied = PermissionSet.none();

                                return channel
                                        .addMemberOverwrite(
                                                playerId,
                                                PermissionOverwrite.forMember(playerId, allowed, denied)
                                        );
                            });
                })
                .then();
    }

}
