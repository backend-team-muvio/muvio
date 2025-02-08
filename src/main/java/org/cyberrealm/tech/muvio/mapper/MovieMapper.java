package org.cyberrealm.tech.muvio.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import org.cyberrealm.tech.muvio.config.MapperConfig;
import org.cyberrealm.tech.muvio.dto.ActorDto;
import org.cyberrealm.tech.muvio.dto.MovieDto;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.cyberrealm.tech.muvio.model.Movie;
import org.cyberrealm.tech.muvio.model.Photo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface MovieMapper {
    @Mapping(source = "rating.rating", target = "rating")
    @Mapping(source = "duration.duration", target = "duration")
    @Mapping(source = "producer.name", target = "producer")
    @Mapping(source = "photos", target = "photos", qualifiedByName = "toPhotosDto")
    @Mapping(source = "actors", target = "actorsDto", qualifiedByName = "toActorDto")
    @Mapping(source = "reviews", target = "reviewsDto")
    @Mapping(source = "releaseYear.year", target = "releaseYear")
    @Mapping(source = "genres", target = "genresDto", qualifiedByName = "toGenresDto")
    @Mapping(source = "posterPath.path", target = "posterPath")
    MovieDto toMovieDto(Movie movie);

    @Named("toGenresDto")
    default Set<String> toGenresDto(Set<GenreEntity> genres) {
        return genres.stream()
                .map(GenreEntity::getName)
                .collect(Collectors.toSet());
    }

    @Named("toActorDto")
    default Set<ActorDto> toActorDto(Set<Actor> actor) {
        return actor.stream()
                .map(a -> new ActorDto(a.getName(), a.getPhoto())).collect(Collectors.toSet());
    }

    @Named("toPhotosDto")
    default Set<String> toPhotosDto(Set<Photo> photos) {
        return photos.stream().map(Photo::getPath).collect(Collectors.toSet());
    }
}
