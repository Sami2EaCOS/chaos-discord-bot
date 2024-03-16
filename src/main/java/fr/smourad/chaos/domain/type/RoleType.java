package fr.smourad.chaos.domain.type;

import discord4j.rest.util.Permission;
import lombok.Getter;

import java.util.List;

@Getter
public enum RoleType {

    MODIFY_EMOJI(Permission.MANAGE_GUILD_EXPRESSIONS),
    MOVE_MEMBERS(Permission.MOVE_MEMBERS),
    TAG_EVERYONE(Permission.MENTION_EVERYONE);

    private final List<Permission> permissions;

    RoleType(Permission... permissions) {
        this.permissions = List.of(permissions);
    }

}
