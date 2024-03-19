package fr.smourad.chaos.command.resource;

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import fr.smourad.chaos.command.DiscordCommand;
import fr.smourad.chaos.domain.type.LootType;
import fr.smourad.chaos.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class RecapCommand extends DiscordCommand {

    private final PlayerService service;

    @Override
    public String getName() {
        return "chaos-recap";
    }

    @Override
    protected String getDescription() {
        return "Get a recap of your progression";
    }

    @Override
    protected List<ApplicationCommandOptionData> getOptions() {
        return List.of();
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        return event.getInteraction().getGuild()
                .flatMap(guild -> service.get(event.getInteraction().getUser(), guild))
                .flatMap(player -> {
                    BigInteger oneLevel = BigInteger.valueOf(10_000);

                    return event.reply(
                            InteractionApplicationCommandCallbackSpec.builder()
                                    .addEmbed(EmbedCreateSpec.builder()
                                            .addField(EmbedCreateFields.Field
                                                    .of("level", player.getExperience().divide(oneLevel).toString(), false)
                                            )
                                            .addField(EmbedCreateFields.Field
                                                    .of("XP before next level", oneLevel.subtract(player.getExperience().mod(oneLevel)).toString(), false)
                                            )
                                            .addField(EmbedCreateFields.Field
                                                    .of("boxes", player.getBoxes().toString(), false)
                                            )
                                            .build()
                                    )
                                    .addEmbed(EmbedCreateSpec.builder()
                                            .addAllFields(Stream
                                                    .of(LootType.values())
                                                    .map(lootType -> EmbedCreateFields.Field
                                                            .of(
                                                                    lootType.name(),
                                                                    Objects.requireNonNullElse(player.getLoots().get(lootType), 0).toString(),
                                                                    false
                                                            )
                                                    )
                                                    .toList()
                                            )
                                            .build()
                                    )
                                    .build()
                    );
                })
                .then();
    }

    @Override
    public Mono<Void> autocomplete(ChatInputAutoCompleteEvent event) {
        return Mono.empty();
    }

}
