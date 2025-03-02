package org.cyberrealm.tech.muvio.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    @DBRef
    private Set<GenreEntity> genres = new HashSet<>();
    @DBRef
    private Rating rating;
    private String trailer;
    @DBRef
    private Photo posterPath;
    @DBRef
    private Duration duration;
    @DBRef
    private Producer producer;
    @DBRef
    private Set<Photo> photos = new HashSet<>();
    @DBRef
    private Set<Actor> actors = new HashSet<>();
    @DBRef
    private List<Review> reviews = new ArrayList<>();
    @DBRef
    private Year releaseYear;
    private String overview;
    private Set<Vibe> vibes = new HashSet<>();
    private Set<Category> categories = new HashSet<>();
}
