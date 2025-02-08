package org.cyberrealm.tech.muvio.model;

import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "reviews")
public class Review {
    @Id
    private String id;
    private String nickname;
    private String text;
    private LocalDateTime time;
}
