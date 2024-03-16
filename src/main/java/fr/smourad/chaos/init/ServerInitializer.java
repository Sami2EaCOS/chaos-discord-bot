package fr.smourad.chaos.init;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.spec.RoleCreateSpec;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import fr.smourad.chaos.domain.type.RoleType;
import fr.smourad.chaos.event.DiscordEventHandler;
import fr.smourad.chaos.event.DiscordEventListener;
import fr.smourad.chaos.service.ServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ServerInitializer implements DiscordEventListener {

    private final ServerService serverService;

    @DiscordEventHandler
    public Mono<Void> onJoin(GuildCreateEvent event) {
        Guild guild = event.getGuild();

        return serverService.get(guild)
                .flatMap(server -> Flux
                        .fromArray(RoleType.values())
                        .flatMap(roleType -> {
                            if (server.getRoles().containsKey(roleType)) {
                                return Mono.empty();
                            } else {
                                return guild
                                        .createRole(
                                                RoleCreateSpec.builder()
                                                        .name(roleType.name())
                                                        .permissions(PermissionSet.of(roleType.getPermissions().toArray(new Permission[]{})))
                                                        .build()
                                        )
                                        .doOnNext(role -> server.getRoles().put(roleType, role.getId().asBigInteger()));
                            }
                        })
                        .then(serverService.save(server))
                        .then()
                );
    }

}
