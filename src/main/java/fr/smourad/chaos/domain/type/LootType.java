package fr.smourad.chaos.domain.type;

import lombok.Getter;

@Getter
public enum LootType {

    CHANGE_NAME(100),
    MOVE_VOICE_CHANNEL(10),
    KICK_VOICE_CHANNEL(1),
    ;

    private final Integer weight;

    LootType(int weight) {
        this.weight = weight;
    }

}
