package org.cyberrealm.tech.muvio.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.Actor;

public record MediaDtoFromDb(String id, String title, Set<String> genres, Double rating,
                             String trailer, String posterPath, String duration, String director,
                             Set<String> photos, Map<String, Actor> actors,
                             List<ReviewDto> reviews, Integer releaseYear, String overview,
                             String type) {
}
