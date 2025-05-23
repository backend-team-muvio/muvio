package org.cyberrealm.tech.muvio.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cyberrealm.tech.muvio.service.impl.SmartDoubleSerializer;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaBaseDto {
    private String id;
    private String title;
    private Set<String> genres;
    @JsonSerialize(using = SmartDoubleSerializer.class)
    private Double rating;
    private String posterPath;
    private String duration;
    private Integer releaseYear;
    private String type;
}
