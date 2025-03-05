package org.cyberrealm.tech.muvio.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Data
@Document(collection = "reviews")
public class Review {
    @Id
    private String id;
    private String author;
    private String avatarPath;
    private String content;
    private LocalDateTime time;
}
