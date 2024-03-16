package fr.smourad.chaos.domain;

import fr.smourad.chaos.domain.type.RoleType;
import jakarta.annotation.Nonnull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Data
@Document("server")
public class Server {

    @Id
    @Nonnull
    private UUID id;

    @Version
    private Long version;

    @Nonnull
    private BigInteger guildId;

    private Map<RoleType, BigInteger> roles = new EnumMap<>(RoleType.class);

}
