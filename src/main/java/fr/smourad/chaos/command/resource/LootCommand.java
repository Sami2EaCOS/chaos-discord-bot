package fr.smourad.chaos.command.resource;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import fr.smourad.chaos.command.DiscordCommand;
import fr.smourad.chaos.service.LootService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LootCommand extends DiscordCommand {

    private final LootService service;

    @Override
    public String getName() {
        return "loot";
    }

    @Override
    protected String getDescription() {
        return "Loot something from loot box you got";
    }

    @Override
    protected List<ApplicationCommandOptionData> getOptions() {
        return List.of();
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        return event.getInteraction().getGuild()
                .flatMap(guild -> service.loot(event, event.getInteraction().getUser(), guild));
    }

}
