package org.cyberrealm.tech.muvio.dto;

import java.util.List;
import java.util.Set;

public record MovieDto(String id, String name, Set<String> genresDto, String rating, String trailer,
                       String posterPath, String duration, String producer, Set<String> photos,
                       Set<ActorDto> actorsDto, List<ReviewDto> reviewsDto, String releaseYear,
                       String overview) {
}
