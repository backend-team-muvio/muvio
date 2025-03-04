package org.cyberrealm.tech.muvio.mapper;

import info.movito.themoviedbapi.model.movies.MovieDb;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.cyberrealm.tech.muvio.config.MapperConfig;
import org.cyberrealm.tech.muvio.dto.ActorDto;
import org.cyberrealm.tech.muvio.dto.MovieDto;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.cyberrealm.tech.muvio.model.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface MovieMapper {
    @Mapping(source = "actors", target = "actorsDto", qualifiedByName = "toActorDto")
    @Mapping(source = "reviews", target = "reviewsDto")
    @Mapping(source = "genres", target = "genresDto", qualifiedByName = "toGenresDto")
    @Mapping(source = "duration", target = "duration", qualifiedByName = "toDuration")
    MovieDto toMovieDto(Movie movie);

    @Mapping(target = "id", expression = "java(String.valueOf(movieDb.getId()))")
    @Mapping(source = "movieDb.runtime", target = "duration")
    @Mapping(source = "movieDb.voteAverage", target = "rating")
    Movie toEntity(MovieDb movieDb);

    @Named("toGenresDto")
    default Set<String> toGenresDto(Set<GenreEntity> genres) {
        return genres.stream()
                .map(GenreEntity::getName)
                .collect(Collectors.toSet());
    }

    @Named("toActorDto")
    default Set<ActorDto> toActorDto(Set<Actor> actors) {
        return actors.stream()
                .map(actor -> new ActorDto(actor.getName(), actor.getPhoto()))
                .collect(Collectors.toSet());
    }

    @Named("toDuration")
    default String toDuration(Integer duration) {
        long hours = TimeUnit.MINUTES.toHours(duration);
        long minutes = duration - TimeUnit.HOURS.toMinutes(hours);
        return String.format("%dh %02dm", hours, minutes);
    }
}
