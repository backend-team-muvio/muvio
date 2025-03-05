package org.cyberrealm.tech.muvio.model;

import java.util.HashMap;
import java.util.Map;

public enum GenreEn {
    MUSIC("Music"), MYSTERY("Mystery"), ROMANCE("Romance"), SCIENCE_FICTION("Science Fiction"),
    TV_MOVIE("TV Movie"), THRILLER("Thriller"), WAR("War"), WESTERN("Western"), CRIME("Crime"),
    DOCUMENTARY("Documentary"), DRAMA("Drama"), FAMILY("Family"), FANTASY("Fantasy"),
    HISTORY("History"), HORROR("Horror"), ACTION("Action"), ADVENTURE("Adventure"),
    ANIMATION("Animation"), COMEDY("Comedy");

    private final String name;
    private static final Map<String, GenreEn> NAME_TO_ENUM = new HashMap<>();

    static {
        for (GenreEn genreEn : values()) {
            NAME_TO_ENUM.put(genreEn.name, genreEn);
        }
    }

    GenreEn(String name) {
        this.name = name;
    }

    public static GenreEn fromString(String name) {
        return NAME_TO_ENUM.get(name);
    }
}

