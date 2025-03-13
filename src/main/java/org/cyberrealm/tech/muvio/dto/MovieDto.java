package org.cyberrealm.tech.muvio.dto;

import java.util.List;
import java.util.Set;

public record MovieDto(
        String id, String title, Set<String> genresDto, double rating, String trailer,
        String posterPath, String duration, String director, Set<String> photos,
        Set<ActorDto> actorsDto, List<ReviewDto> reviewsDto, int releaseYear,
        String overview, Set<String> keywords) {
}
