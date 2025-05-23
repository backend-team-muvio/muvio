package org.cyberrealm.tech.muvio.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import java.util.Set;
import org.cyberrealm.tech.muvio.service.impl.SmartDoubleSerializer;

public record MediaDtoWithCast(String id, String title, Integer releaseYear, Set<String> genres,
                               @JsonSerialize(using = SmartDoubleSerializer.class) Double rating,
                               String posterPath, String duration, String director,
                               List<String> actors) {
}
