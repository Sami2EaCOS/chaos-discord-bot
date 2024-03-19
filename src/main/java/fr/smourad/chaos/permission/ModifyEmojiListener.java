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
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModifyEmojiListener implements DiscordEventListener {

    private final PlayerService playerService;
    private final ActionService actionService;

    @DiscordEventHandler
    @Transactional
    public Mono<Void> onEmojiEdit(AuditLogEntryCreateEvent event) {
        AuditLogEntry auditLogEntry = event.getAuditLogEntry();

        if (!List.of(
                ActionType.EMOJI_CREATE,
                ActionType.EMOJI_DELETE
        ).contains(auditLogEntry.getActionType())) {
            return Mono.empty();
        }

        Optional<Snowflake> responsibleUserId = auditLogEntry.getResponsibleUserId();

        return responsibleUserId
                .map(snowflake -> event.getGuild()
                .flatMap(guild -> playerService
                        .get(snowflake, guild.getId())
                        .flatMap(player -> actionService.modifyEmoji(player, guild))
                )).orElseGet(Mono::empty);

    }

}
