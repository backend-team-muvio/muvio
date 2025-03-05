package org.cyberrealm.tech.muvio.model;

import java.util.*;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "movies")
public class Movie {
    @Id
    private String id;
    private String name;
    private Set<GenreEn> genres = new HashSet<>();
    private Double rating;
    private String trailer;
    private String posterPath;
    private Integer duration;
    private String director;
    private Set<String> photos = new HashSet<>();
    @DBRef
    private Map<String, Actor> actors = new HashMap<>();
    @DBRef
    private List<ReviewDb> reviewDbs = new ArrayList<>();
    private Integer releaseYear;
    private String overview;
    private Set<Vibe> vibes = new HashSet<>();
    private Set<Category> categories = new HashSet<>();
    private Type type;
}
