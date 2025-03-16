package org.cyberrealm.tech.muvio.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private Set<GenreEntity> genres = new HashSet<>();
    private Double rating;
    private String trailer;
    private String posterPath;
    private Integer duration;
    private String director;
    private Set<String> photos;
    @DBRef
    private Map<String, Actor> actors = new HashMap<>();
    private List<Review> reviews;
    private Integer releaseYear;
    private String overview;
    private Set<Vibe> vibes = new HashSet<>();
    private Set<Category> categories = new HashSet<>();
    private Type type;
    private Set<TopLists> topLists;
}
