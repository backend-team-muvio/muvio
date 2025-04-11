package org.cyberrealm.tech.muvio.mapper;

import static org.cyberrealm.tech.muvio.common.Constants.AMPERSAND;

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
        if (genres == null || genres.isEmpty()) {
            return null;
        }
        return genres.stream().map(genre -> genre.getName().contains(AMPERSAND)
                ? genre.getName().split(AMPERSAND)
                : new String[]{genre.getName()})
                .flatMap(Arrays::stream)
                .map(genre -> GenreEntity.fromString(genre.trim()))
                .collect(Collectors.toSet());
    }
}
