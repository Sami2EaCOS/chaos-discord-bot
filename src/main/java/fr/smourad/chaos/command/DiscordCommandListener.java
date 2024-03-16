package fr.smourad.chaos.command;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import fr.smourad.chaos.event.DiscordEventHandler;
import fr.smourad.chaos.event.DiscordEventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DiscordCommandListener implements DiscordEventListener {

    private final Map<String, DiscordCommand> commands;
    private final RestClient client;

    public DiscordCommandListener(RestClient client, List<DiscordCommand> commands) {
        this.client = client;
        this.commands = commands.stream()
                .collect(Collectors.toMap(DiscordCommand::getName, c -> c));

        init(commands);
    }

    @DiscordEventHandler
    public Mono<Void> listen(ChatInputInteractionEvent event) {
        String commandName = event.getCommandName();

        if (commands.containsKey(commandName)) {
            return commands.get(commandName).execute(event);
        }

        return Mono.empty();
    }

    @DiscordEventHandler
    public Mono<Void> autocomplete(ChatInputAutoCompleteEvent event) {
        String commandName = event.getCommandName();

        if (commands.containsKey(commandName)) {
            return commands.get(commandName).autocomplete(event);
        }

        return Mono.empty();
    }

    public void init(List<DiscordCommand> commands) {
        Long applicationId = client.getApplicationId().block();

        for (DiscordCommand command : commands) {
            ApplicationCommandRequest commandRequest = ApplicationCommandRequest.builder()
                    .name(command.getName())
                    .description(command.getDescription())
                    .addAllOptions(command.getOptions())
                    .build();

            client.getApplicationService()
                    .createGlobalApplicationCommand(applicationId, commandRequest)
                    .subscribe();
        }
    }

}
