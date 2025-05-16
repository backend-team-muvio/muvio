package org.cyberrealm.tech.muvio.dto;

import java.util.List;
import java.util.Set;

public record MediaDto(
        String id, String title, Set<String> genres, Double rating, String trailer,
        String posterPath, String duration, String director, Set<String> photos,
        List<ActorDto> actors, List<ReviewDto> reviews, Integer releaseYear,
        List<String> countries, String overview, String type) {
}
