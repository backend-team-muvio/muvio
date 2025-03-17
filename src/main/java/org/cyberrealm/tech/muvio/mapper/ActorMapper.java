package org.cyberrealm.tech.muvio.mapper;

import info.movito.themoviedbapi.model.movies.Cast;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.cyberrealm.tech.muvio.config.MapperConfig;
import org.cyberrealm.tech.muvio.dto.ActorDto;
import org.cyberrealm.tech.muvio.model.Actor;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface ActorMapper {
    Actor toActorEntity(Cast cast);

    @Named("toActorDto")
    default Set<ActorDto> toActorDto(Map<String, Actor> actors) {
        return actors.entrySet().stream()
                .map(actor -> new ActorDto(actor.getKey(), actor.getValue().getName(),
                        actor.getValue().getPhoto()))
                .collect(Collectors.toSet());
    }

    @Named("toSetActors")
    default Set<String> toSetActors(Map<String, Actor> actors) {
        return actors.values().stream()
                .map(Actor::getName)
                .collect(Collectors.toSet());
    }
}
