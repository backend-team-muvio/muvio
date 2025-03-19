package org.cyberrealm.tech.muvio.dto;

import lombok.Data;

@Data
public class MediaBaseDto {
    private String id;
    private String title;
    private String genres;
    private Double rating;
    private String posterPath;
    private String duration;
    private Integer releaseYear;
    private String type;
}
