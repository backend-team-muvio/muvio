package org.cyberrealm.tech.muvio.mapper;

import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import java.util.concurrent.TimeUnit;
import org.cyberrealm.tech.muvio.config.MapperConfig;
import org.cyberrealm.tech.muvio.dto.MediaDto;
import org.cyberrealm.tech.muvio.dto.MediaDtoFromDb;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCast;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCastFromDb;
import org.cyberrealm.tech.muvio.model.Media;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class, uses = {ActorMapper.class, GenreMapper.class})
public interface MediaMapper {
    String TV = "TV";

    @Mapping(source = "actors", target = "actors", qualifiedByName = "toActorDto")
    @Mapping(source = "duration", target = "duration", qualifiedByName = "toDuration")
    MediaDto toMovieDto(MediaDtoFromDb movie);

    @Mapping(target = "id", expression = "java(String.valueOf(movieDb.getId()))")
    @Mapping(source = "movieDb.runtime", target = "duration")
    @Mapping(source = "movieDb.voteAverage", target = "rating")
    Media toEntity(MovieDb movieDb);

    @Mapping(source = "tvSeriesDb.name", target = "title")
    @Mapping(source = "tvSeriesDb.lastEpisodeToAir.runtime", target = "duration")
    @Mapping(source = "tvSeriesDb.voteAverage", target = "rating")
    @Mapping(source = "genres", target = "genres", ignore = true)
    @Mapping(source = "type", target = "type", ignore = true)
    @Mapping(source = "id", target = "id", qualifiedByName = "setTvSeriesId")
    Media toEntity(TvSeriesDb tvSeriesDb);

    @Mapping(source = "actors", target = "actors", qualifiedByName = "toSetActors")
    @Mapping(source = "duration", target = "duration", qualifiedByName = "toDuration")
    MediaDtoWithCast toMediaDtoWithCast(MediaDtoWithCastFromDb movie);

    @Named("toDuration")
    default String toDuration(Integer duration) {
        long hours = TimeUnit.MINUTES.toHours(duration);
        long minutes = duration - TimeUnit.HOURS.toMinutes(hours);
        return String.format("%dh %02dm", hours, minutes);
    }

    @Named("setTvSeriesId")
    default String setTvSeriesId(Integer id) {
        return TV + id;
    }
}
