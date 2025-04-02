package org.cyberrealm.tech.muvio.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
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
    private Set<String> photos;
    private List<RoleActor> actors;
    private List<Review> reviews;
    private Set<Vibe> vibes = new HashSet<>();
    private Set<Category> categories = new HashSet<>();
    private Set<TopLists> topLists;
}
