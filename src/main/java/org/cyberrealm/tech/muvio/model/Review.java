package org.cyberrealm.tech.muvio.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Review {
    private String id;
    private String author;
    private String avatarPath;
    private String content;
    private LocalDateTime time;
    private Double rating;
}
