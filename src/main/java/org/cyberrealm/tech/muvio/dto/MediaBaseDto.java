package org.cyberrealm.tech.muvio.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaBaseDto {
    private String id;
    private String title;
    private Set<String> genres;
    private Double rating;
    private String posterPath;
    private String duration;
    private Integer releaseYear;
    private String type;
}
