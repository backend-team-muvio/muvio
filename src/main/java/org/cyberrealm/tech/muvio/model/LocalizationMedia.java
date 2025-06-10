package org.cyberrealm.tech.muvio.model;

import java.util.List;
import java.util.Set;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "localization_media")
public class LocalizationMedia {
    @Id
    private String id;
    @TextIndexed
    private String title;
    private String overview;
    private List<String> countries;
    private String posterPath;
    private String trailer;
    private String duration;
    private String type;
    private Set<String> genres;
    private List<Review> reviews;
}
