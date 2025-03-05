package org.cyberrealm.tech.muvio.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import org.cyberrealm.tech.muvio.config.MapperConfig;
import org.cyberrealm.tech.muvio.dto.ActorDto;
import org.cyberrealm.tech.muvio.dto.MovieDto;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface MovieMapper {

//    @Mapping(source = "actors", target = "actorsDto", qualifiedByName = "toActorDto")
    MovieDto toMovieDto(Movie movie);

//    @Named("toActorDto")
//    default Set<ActorDto> toActorDto(Set<Actor> actor) {
//        return actor.stream()
//                .map(a -> new ActorDto(a.getName(), a.getPhoto())).collect(Collectors.toSet());
//    }

}
