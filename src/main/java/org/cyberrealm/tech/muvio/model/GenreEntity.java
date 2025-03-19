package org.cyberrealm.tech.muvio.model;

import java.util.HashMap;
import java.util.Map;

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
    SCIENCE_FICTION("Science Fiction"),
    SCRIPTED("Scripted"),
    SOAP("Soap"),
    TALK_SHOW("Talk Show"),
    THRILLER("Thriller"),
    TV_MOVIE("TV Movie"),
    WAR("War"),
    WESTERN("Western");


    private static final Map<String, GenreEntity> NAME_TO_ENUM = new HashMap<>();
    private final String name;

    static {
        for (GenreEntity genreEntity : values()) {
            NAME_TO_ENUM.put(genreEntity.name, genreEntity);
        }
    }

    GenreEntity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static GenreEntity fromString(String name) {
        return NAME_TO_ENUM.get(name);
    }
}

