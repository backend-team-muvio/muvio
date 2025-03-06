package org.cyberrealm.tech.muvio.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "actors")
public class Actor {
    @Id
    private String name;
    private String photo;
}
