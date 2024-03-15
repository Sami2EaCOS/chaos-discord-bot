package fr.smourad.chaos.domain;

import discord4j.common.util.Snowflake;
import fr.smourad.chaos.domain.type.LootType;
import jakarta.annotation.Nonnull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Document("user")
@Data
@CompoundIndex(def = "{'discordId': 1, 'guildId': 1}", unique = true)
public class Player {

    @Id
    @Nonnull
    private UUID id;

    @Nonnull
    private BigInteger discordId;

    @Nonnull
    private BigInteger guildId;

    private Integer boxes = 0;
    private BigInteger experience = BigInteger.ZERO;

    private Map<LootType, Long> loots = new EnumMap<>(LootType.class);

}
