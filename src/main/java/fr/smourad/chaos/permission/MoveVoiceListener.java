package fr.smourad.chaos.permission;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.AuditLogEntryCreateEvent;
import discord4j.core.object.audit.ActionType;
import discord4j.core.object.audit.AuditLogEntry;
import fr.smourad.chaos.event.DiscordEventHandler;
import fr.smourad.chaos.event.DiscordEventListener;
import fr.smourad.chaos.service.ActionService;
import fr.smourad.chaos.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MoveVoiceListener implements DiscordEventListener {

    private final PlayerService playerService;
    private final ActionService actionService;

    @DiscordEventHandler
    public Mono<Void> onMove(AuditLogEntryCreateEvent event) {
        AuditLogEntry auditLogEntry = event.getAuditLogEntry();
        if (!Objects.equals(auditLogEntry.getActionType(), ActionType.MEMBER_MOVE)) {
            return Mono.empty();
        }

        Optional<Snowflake> responsibleUserId = auditLogEntry.getResponsibleUserId();
        Optional<Snowflake> targetId = auditLogEntry.getTargetId();

        if (targetId.isEmpty() || responsibleUserId.isEmpty()) {
            return Mono.empty();
        }

        return event.getGuild()
                .flatMap(guild -> Mono
                        .zip(
                                playerService
                                        .get(responsibleUserId.get(), guild.getId()),
                                playerService
                                        .get(targetId.get(), guild.getId())
                        )
                        .flatMap(tuple -> actionService.move(tuple.getT1(), tuple.getT2(), guild))
                );

    }

}
