package org.cyberrealm.tech.muvio.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "movies")
public class Movie {
    @Id
    private String id;
    private String title;
    @DBRef
    private Set<GenreEntity> genres = new HashSet<>();
    private Double rating;
    private String trailer;
    private String posterPath;
    private Integer duration;
    private String producer;
    private Set<String> photos = new HashSet<>();
    @DBRef
    private Set<Actor> actors = new HashSet<>();
    private List<Review> reviews = new ArrayList<>();
    private Integer releaseYear;
    private String overview;
    private Set<Vibe> vibes = new HashSet<>();
    private Set<Category> categories = new HashSet<>();
}
