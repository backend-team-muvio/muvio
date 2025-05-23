package org.cyberrealm.tech.muvio.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import java.util.Set;
import org.cyberrealm.tech.muvio.service.impl.SmartDoubleSerializer;

public record MediaDto(
        String id, String title, Set<String> genres,
        @JsonSerialize(using = SmartDoubleSerializer.class) Double rating, String trailer,
        String posterPath, String duration, String director, Set<String> photos,
        List<ActorDto> actors, List<ReviewDto> reviews, Integer releaseYear,
        List<String> countries, String overview, String type) {
}
