package org.cyberrealm.tech.muvio.dto;

import java.util.List;
import java.util.Set;

public record MediaDtoWithPoints(String id, String title, Set<String> genres, Double rating,
                                 String trailer, String posterPath, String duration,
                                 String director, Set<String> photos, List<ActorDto> actors,
                                 List<ReviewDto> reviews, Integer releaseYear, String overview,
                                 String type, Integer points) {
}
