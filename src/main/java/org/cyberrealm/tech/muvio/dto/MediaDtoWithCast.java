package org.cyberrealm.tech.muvio.dto;

import java.util.List;
import java.util.Set;

public record MediaDtoWithCast(String id, String title, Set<String> genres, Double rating,
                               String posterPath, String duration, String director,
                               List<String> actors) {
}
