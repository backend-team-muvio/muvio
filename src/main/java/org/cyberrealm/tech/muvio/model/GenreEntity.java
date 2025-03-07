package org.cyberrealm.tech.muvio.model;

import java.util.HashMap;
import java.util.Map;

public enum GenreEntity {
    MUSIC("Music"), MYSTERY("Mystery"), ROMANCE("Romance"), SCIENCE_FICTION("Science Fiction"),
    TV_MOVIE("TV Movie"), THRILLER("Thriller"), WAR("War"), WESTERN("Western"), CRIME("Crime"),
    DOCUMENTARY("Documentary"), DRAMA("Drama"), FAMILY("Family"), FANTASY("Fantasy"),
    HISTORY("History"), HORROR("Horror"), ACTION("Action"), ADVENTURE("Adventure"),
    ANIMATION("Animation"), COMEDY("Comedy");

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

    public static GenreEntity fromString(String name) {
        return NAME_TO_ENUM.get(name);
    }
}

