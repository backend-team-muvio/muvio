package org.cyberrealm.tech.muvio.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "producers")
public class Producer {
    @Id
    private String name;
}
