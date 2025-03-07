package org.cyberrealm.tech.muvio.mapper;

import info.movito.themoviedbapi.model.core.Genre;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.cyberrealm.tech.muvio.config.MapperConfig;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface GenreMapper {

    default GenreEntity toGenreEntity(Genre genre) {
        return genre != null ? GenreEntity.fromString(genre.getName()) : null;
    }

    @Named("mapGenres")
    default Set<GenreEntity> toGenreEntitySet(List<Genre> genres) {
        return genres == null ? Set.of() : genres.stream()
                .map(this::toGenreEntity)
                .collect(Collectors.toSet());
    }
}
