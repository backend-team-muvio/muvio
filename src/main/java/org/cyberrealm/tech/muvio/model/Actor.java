package org.cyberrealm.tech.muvio.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "actors")
public class Actor {
    @Id
    private String name;
    private String photo;
}
