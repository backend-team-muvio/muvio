package org.cyberrealm.tech.muvio.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public enum Type {
    MOVIE("Movie"), TV_SHOW("TV Show"), SHORTS("Shorts"),;

    private static final Map<String, Type> NAME_TO_ENUM = new HashMap<>();
    private final String name;

    static {
        for (Type type : values()) {
            NAME_TO_ENUM.put(type.getName().toLowerCase(), type);
        }
    }

    Type(String name) {
        this.name = name;
    }

    public static Type fromString(String name) {
        return NAME_TO_ENUM.get(name.toLowerCase());
    }
}
