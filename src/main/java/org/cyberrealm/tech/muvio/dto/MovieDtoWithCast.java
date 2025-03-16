package org.cyberrealm.tech.muvio.dto;

import java.util.Set;

public record MovieDtoWithCast(String id, String title, Set<String> genres, Double rating,
                                String posterPath, String duration, String director,
                                Set<String> actors) {
}
