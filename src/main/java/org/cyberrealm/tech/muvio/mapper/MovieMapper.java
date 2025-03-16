package org.cyberrealm.tech.muvio.mapper;

import info.movito.themoviedbapi.model.movies.MovieDb;
import java.util.concurrent.TimeUnit;
import org.cyberrealm.tech.muvio.config.MapperConfig;
import org.cyberrealm.tech.muvio.dto.MovieDto;
import org.cyberrealm.tech.muvio.dto.MovieDtoFromDb;
import org.cyberrealm.tech.muvio.dto.MovieDtoWithCast;
import org.cyberrealm.tech.muvio.dto.MovieDtoWithCastFromDb;
import org.cyberrealm.tech.muvio.model.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class, uses = {ActorMapper.class, GenreMapper.class})
public interface MovieMapper {
    @Mapping(source = "actors", target = "actors", qualifiedByName = "toActorDto")
    @Mapping(source = "duration", target = "duration", qualifiedByName = "toDuration")
    MovieDto toMovieDto(MovieDtoFromDb movie);

    @Mapping(target = "id", expression = "java(String.valueOf(movieDb.getId()))")
    @Mapping(source = "movieDb.runtime", target = "duration")
    @Mapping(source = "movieDb.voteAverage", target = "rating")
    Movie toEntity(MovieDb movieDb);

    @Mapping(source = "actors", target = "actors", qualifiedByName = "toSetActors")
    @Mapping(source = "duration", target = "duration", qualifiedByName = "toDuration")
    MovieDtoWithCast toMovieDtoWithCast(MovieDtoWithCastFromDb movie);

    @Named("toDuration")
    default String toDuration(Integer duration) {
        long hours = TimeUnit.MINUTES.toHours(duration);
        long minutes = duration - TimeUnit.HOURS.toMinutes(hours);
        return String.format("%dh %02dm", hours, minutes);
    }
}
