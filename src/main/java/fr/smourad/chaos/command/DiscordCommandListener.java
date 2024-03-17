package fr.smourad.chaos.command;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import fr.smourad.chaos.event.DiscordEventHandler;
import fr.smourad.chaos.event.DiscordEventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
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

        client.getApplicationId().flatMapMany(applicationId -> client
                .getApplicationService()
                .getGlobalApplicationCommands(applicationId)
                .flatMap(command -> client.getApplicationService().deleteGlobalApplicationCommand(applicationId, command.id().asLong()))
        ).subscribe();
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

    @DiscordEventHandler
    public Mono<Void> init(GuildCreateEvent event) {
        long guildId = event.getGuild().getId().asLong();

        return client.getApplicationId().flatMapMany(applicationId -> client
                .getApplicationService()
                .getGuildApplicationCommands(applicationId, guildId)
                .flatMap(command -> client.getApplicationService()
                        .deleteGuildApplicationCommand(applicationId, guildId, command.id().asLong())
                ))
                .then(client.getApplicationId()
                        .flatMapMany(applicationId -> Flux.fromIterable(commands.values())
                                .flatMap(command -> {
                                            ApplicationCommandRequest commandRequest = ApplicationCommandRequest.builder()
                                                    .name(command.getName())
                                                    .description(command.getDescription())
                                                    .addAllOptions(command.getOptions())
                                                    .build();

                                            return client.getApplicationService()
                                                    .createGuildApplicationCommand(applicationId, guildId, commandRequest);
                                        }
                                ))
                        .then()
                );
    }

}
