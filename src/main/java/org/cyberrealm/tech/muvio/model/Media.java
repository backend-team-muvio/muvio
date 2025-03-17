package org.cyberrealm.tech.muvio.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "media")
public class Media {
    @Id
    private String id;
    private String title;
    private String overview;
    private Integer releaseYear;
    private String posterPath;
    private String trailer;
    private Double rating;
    private Integer duration;
    private String director;
    private Type type;

    private Set<GenreEntity> genres = new HashSet<>();
    private Set<String> photos = new HashSet<>();

    @DBRef
    private Map<String, Actor> actors = new HashMap<>();
    private List<Review> reviews = new ArrayList<>();
    private Set<Vibe> vibes = new HashSet<>();
    private Set<Category> categories = new HashSet<>();
    private Type type;
    private Set<TopLists> topLists;
}
