package org.cyberrealm.tech.muvio.dto;

import java.util.Map;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.Actor;

public record MovieDtoWithCastFromDb(String id, String title, Set<String> genres, Double rating,
                                      String posterPath, Integer duration, String director,
                                      Map<String, Actor> actors) {
}
