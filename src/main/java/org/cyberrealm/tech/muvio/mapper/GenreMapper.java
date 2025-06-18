package org.cyberrealm.tech.muvio.mapper;

import static org.cyberrealm.tech.muvio.common.Constants.AMPERSAND;
import static org.cyberrealm.tech.muvio.common.Constants.UNDERSCORE;
import static org.cyberrealm.tech.muvio.common.Constants.WHITE_SPACE;

import info.movito.themoviedbapi.model.core.Genre;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.cyberrealm.tech.muvio.config.MapperConfig;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface GenreMapper {

    @Named("toGenreEntity")
    default Set<GenreEntity> toGenreEntity(List<Genre> genres) {
        return splitAndCleanGenres(genres, AMPERSAND).stream()
                .map(GenreEntity::fromString)
                .collect(Collectors.toSet());
    }

    @Named("toStringGenres")
    default Set<String> toStringGenres(Set<String> genres) {
        if (genres == null || genres.isEmpty()) {
            return Set.of();
        }
        return genres.stream().map(genre -> GenreEntity
                        .fromString(genre.replace(UNDERSCORE, WHITE_SPACE)).getName())
                .collect(Collectors.toSet());
    }

    @Named("fromGenreEntityToString")
    default Set<String> fromGenreEntityToString(Set<GenreEntity> genres) {
        if (genres == null || genres.isEmpty()) {
            return Set.of();
        }
        return genres.stream().map(GenreEntity::getName)
                .collect(Collectors.toSet());
    }

    static Set<String> splitAndCleanGenres(List<Genre> genres, String ampersand) {
        if (genres == null || genres.isEmpty()) {
            return Set.of();
        }
        return genres.stream()
                .map(genre -> genre.getName()
                        .contains(ampersand)
                        ? genre.getName().split(ampersand)
                        : new String[]{genre.getName()})
                .flatMap(Arrays::stream)
                .map(String::trim)
                .collect(Collectors.toSet());
    }
}
