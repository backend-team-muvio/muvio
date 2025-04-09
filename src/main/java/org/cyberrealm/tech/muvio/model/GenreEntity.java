package org.cyberrealm.tech.muvio.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public enum GenreEntity {
    ACTION("Action"),
    ADVENTURE("Adventure"),
    ANIMATION("Animation"),
    COMEDY("Comedy"),
    CRIME("Crime"),
    DOCUMENTARY("Documentary"),
    DRAMA("Drama"),
    FAMILY("Family"),
    FANTASY("Fantasy"),
    HISTORY("History"),
    HORROR("Horror"),
    KIDS("Kids"),
    MUSIC("Music"),
    MYSTERY("Mystery"),
    NEWS("News"),
    POLITICS("Politics"),
    REALITY("Reality"),
    ROMANCE("Romance"),
    SCIENCE_FICTION("Science Fiction", "Sci-Fi"),
    SCRIPTED("Scripted"),
    SOAP("Soap"),
    THRILLER("Thriller"),
    TV_MOVIE("TV Movie"),
    WAR("War"),
    WESTERN("Western"),
    TALK("Talk");


    private static final Map<String, GenreEntity> NAME_TO_ENUM = new HashMap<>();
    private final String name;
    private final String[] aliases;

    static {
        for (GenreEntity genreEntity : values()) {
            NAME_TO_ENUM.put(genreEntity.name.toLowerCase(), genreEntity);
            for (String alias : genreEntity.aliases) {
                NAME_TO_ENUM.put(alias.toLowerCase(), genreEntity);
            }
        }
    }

    GenreEntity(String name, String... aliases) {
        this.name = name;
        this.aliases = aliases;
    }

    public static GenreEntity fromString(String name) {
        return NAME_TO_ENUM.get(name.toLowerCase());
    }
}

