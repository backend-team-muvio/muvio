package org.cyberrealm.tech.muvio.model;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "genres")
public class GenreEntity {
    @Id
    private String id;
    private String name;
}
