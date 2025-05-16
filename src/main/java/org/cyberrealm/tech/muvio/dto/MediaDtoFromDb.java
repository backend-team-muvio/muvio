package org.cyberrealm.tech.muvio.dto;

import java.util.List;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.RoleActor;

public record MediaDtoFromDb(String id, String title, Set<String> genres, Double rating,
                             String trailer, String posterPath, Integer duration, String director,
                             Set<String> photos, List<RoleActor> actors, List<ReviewDto> reviews,
                             Integer releaseYear, List<String> countries, String overview,
                             String type, Set<String> categories) {
}
