package org.cyberrealm.tech.muvio.model;

import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "reviews")
public class ReviewDb {
    @Id
    private String id;
    private String nickname;
    private String text;
    private LocalDateTime time;
    private Double rating;
}
