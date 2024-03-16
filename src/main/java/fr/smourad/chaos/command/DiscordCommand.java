package fr.smourad.chaos.command;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

public abstract class DiscordCommand {

    public abstract String getName();
    protected abstract String getDescription();

    protected abstract List<ApplicationCommandOptionData> getOptions();

    public abstract Mono<Void> execute(ChatInputInteractionEvent event);
    public abstract Mono<Void> autocomplete(ChatInputAutoCompleteEvent event);

    private Object getParameter(ChatInputInteractionEvent event, String key, Function<? super ApplicationCommandInteractionOptionValue, ?> type) {
        return event.getOption(key)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(type)
                .orElse(null);
    }

    protected String getStringParameter(ChatInputInteractionEvent event, String key) {
        return (String) getParameter(event, key, ApplicationCommandInteractionOptionValue::asString);
    }

    protected Long getLongParameter(ChatInputInteractionEvent event, String key) {
        return (Long) getParameter(event, key, ApplicationCommandInteractionOptionValue::asLong);
    }

}
