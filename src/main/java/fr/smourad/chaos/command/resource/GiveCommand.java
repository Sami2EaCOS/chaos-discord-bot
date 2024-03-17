package fr.smourad.chaos.command.resource;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.rest.util.Permission;
import fr.smourad.chaos.command.DiscordCommand;
import fr.smourad.chaos.domain.type.LootType;
import fr.smourad.chaos.service.LootService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class GiveCommand extends DiscordCommand {

    private static final String ARGUMENT_TYPE = "type";

    private final LootService service;

    @Override
    public String getName() {
        return "chaos-give";
    }

    @Override
    protected String getDescription() {
        return "Give a loot";
    }

    @Override
    protected List<ApplicationCommandOptionData> getOptions() {
        return List.of(
                ApplicationCommandOptionData.builder()
                        .name(ARGUMENT_TYPE)
                        .description("Type of loot")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .autocomplete(true)
                        .required(true)
                        .build()
        );
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        User user = event.getInteraction().getUser();

        String type = event.getOption(ARGUMENT_TYPE)
                .flatMap(u -> u.getValue().map(ApplicationCommandInteractionOptionValue::asString))
                .orElse("");

        return event.getInteraction().getGuild()
                .flatMap(guild -> user.asMember(guild.getId())
                        .flatMap(member -> member.getBasePermissions()
                                .flatMap(permission -> {
                                    if (permission.contains(Permission.ADMINISTRATOR)) {
                                        try {
                                            return service.loot(event, user, guild, LootType.valueOf(type));
                                        } catch (IllegalArgumentException e) {
                                            return Mono.empty();
                                        }
                                    } else {
                                        return Mono.empty();
                                    }
                                })
                        )
                );
    }

    @Override
    public Mono<Void> autocomplete(ChatInputAutoCompleteEvent event) {
        ApplicationCommandInteractionOption option = event.getFocusedOption();

        if (Objects.equals(ARGUMENT_TYPE, option.getName())) {
            String value = option.getValue()
                    .map(ApplicationCommandInteractionOptionValue::asString)
                    .orElse("");

            return event
                    .respondWithSuggestions(Stream
                            .of(LootType.values())
                            .map(Enum::name)
                            .filter(name -> name.startsWith(value))
                            .map(name -> ApplicationCommandOptionChoiceData.builder()
                                    .name(name)
                                    .value(name)
                                    .build()
                            )
                            .map(ApplicationCommandOptionChoiceData.class::cast)
                            .toList()
                    )
                    .then();
        }

        return Mono.empty();
    }

}
