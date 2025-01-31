package org.cyberrealm.tech.muvio.model;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "movies")
public class Movie {
    @Id
    private String id;
    private String name;
    private Set<String> genres = new HashSet<>();
    private double rating;
    private String trailer;
}
