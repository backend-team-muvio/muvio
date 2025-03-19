package org.cyberrealm.tech.muvio.mapper;

import info.movito.themoviedbapi.model.core.Genre;
import org.cyberrealm.tech.muvio.config.MapperConfig;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface GenreMapper {

    default GenreEntity toGenreEntity(Genre genre) {
        if (genre == null || genre.getName() == null) {
            return null;
        }
        String genreName = genre.getName().contains("&")
                ? genre.getName().split("&")[1].trim()
                : genre.getName();
        return GenreEntity.fromString(genreName);
    }
}
