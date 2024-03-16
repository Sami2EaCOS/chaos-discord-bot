package fr.smourad.chaos.domain.type;

import lombok.Getter;

@Getter
public enum LootType {

    CHANGE_NAME(100),
    TAG_EVERYONE(100),
    BAN_ONE_HOUR(100),
    MUTE_TEXT(100),
    MUTE_VOCAL(100),
    MOVE_MEMBERS(100),
    MODIFY_EMOJI(100),
    MODIFY_SOUNDBOARD(100)
    ;

    private final Integer weight;

    LootType(int weight) {
        this.weight = weight;
    }

}
