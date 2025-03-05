package org.cyberrealm.tech.muvio.mapper;

import info.movito.themoviedbapi.model.core.Genre;
import org.cyberrealm.tech.muvio.config.MapperConfig;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface GenreMapper {
    GenreEntity toGenreEntity(Genre genre);
}
