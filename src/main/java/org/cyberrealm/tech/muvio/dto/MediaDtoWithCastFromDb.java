package org.cyberrealm.tech.muvio.dto;

import java.util.List;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.RoleActor;

public record MediaDtoWithCastFromDb(String id, String title, Integer releaseYear,
                                     Set<String> genres, Double rating,
                                     String posterPath, Integer duration, String director,
                                     List<RoleActor> actors) {
}
