package org.cyberrealm.tech.muvio.mapper;

import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.cyberrealm.tech.muvio.config.MapperConfig;
import org.cyberrealm.tech.muvio.dto.MediaBaseDto;
import org.cyberrealm.tech.muvio.dto.MediaDto;
import org.cyberrealm.tech.muvio.dto.MediaDtoFromDb;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCast;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCastFromDb;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithPoints;
import org.cyberrealm.tech.muvio.model.Category;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.model.Type;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class, uses = {ActorMapper.class, GenreMapper.class})
public interface MediaMapper {
    String TV = "TV";
    String IMAGE_PATH = "https://image.tmdb.org/t/p/w500";
    int TEN = 10;
    int ZERO = 0;
    int FOUR = 4;
    int SHORT_DURATION = 40;
    int DEFAULT_SERIAL_DURATION = 30;

    @Mapping(source = "actors", target = "actors", qualifiedByName = "toActorDto")
    @Mapping(source = "duration", target = "duration", qualifiedByName = "toDuration")
    MediaDto toMovieDto(MediaDtoFromDb movie);

    @Mapping(target = "id", expression = "java(String.valueOf(movieDb.getId()))")
    @Mapping(source = "movieDb.runtime", target = "duration")
    @Mapping(source = "movieDb.voteAverage", target = "rating")
    @Mapping(source = "genres", target = "genres", qualifiedByName = "toGenreEntity")
    @Mapping(target = "posterPath", expression = "java(IMAGE_PATH + movieDb.getPosterPath())")
    @Mapping(source = "releaseDate", target = "releaseYear", qualifiedByName = "getReleaseYear")
    @Mapping(source = "movieDb.runtime", target = "type", qualifiedByName = "putType")
    Media toEntity(MovieDb movieDb);

    @Mapping(source = "tvSeriesDb.name", target = "title")
    @Mapping(source = "tvSeriesDb.voteAverage", target = "rating")
    @Mapping(target = "type", expression = "java(org.cyberrealm.tech.muvio.model.Type.TV_SHOW)")
    @Mapping(target = "id", expression = "java(TV + tvSeriesDb.getId())")
    @Mapping(source = "genres", target = "genres", qualifiedByName = "toGenreEntity")
    @Mapping(source = "episodeRunTime", target = "duration", qualifiedByName = "getDurations")
    @Mapping(target = "posterPath", expression = "java(IMAGE_PATH + tvSeriesDb.getPosterPath())")
    @Mapping(source = "firstAirDate", target = "releaseYear", qualifiedByName = "getReleaseYear")
    Media toEntity(TvSeriesDb tvSeriesDb);

    @Mapping(source = "actors", target = "actors", qualifiedByName = "toListActors")
    @Mapping(source = "duration", target = "duration", qualifiedByName = "toDuration")
    MediaDtoWithCast toMediaDtoWithCast(MediaDtoWithCastFromDb movie);

    @Mapping(source = "media.actors", target = "actors", qualifiedByName = "toActorDto")
    @Mapping(source = "media.duration", target = "duration", qualifiedByName = "toDuration")
    @Mapping(target = "points", expression = "java(calculatePoints(media, categories))")
    MediaDtoWithPoints toMediaDtoWithPoints(Media media, Set<String> categories);

    @Mapping(source = "duration", target = "duration", qualifiedByName = "toDuration")
    MediaBaseDto toMediaBaseDto(Media media);

    @Named("toDuration")
    default String toDuration(Integer duration) {
        long hours = TimeUnit.MINUTES.toHours(duration);
        long minutes = duration - TimeUnit.HOURS.toMinutes(hours);
        return hours > 0 ? String.format("%dh %02dm", hours, minutes)
                : String.format("%02dm", minutes);
    }

    default int calculatePoints(Media media, Set<String> categories) {
        int points = 0;
        if (categories != null && !categories.isEmpty()) {
            for (Category category : media.getCategories()) {
                if (categories.contains(category.name())) {
                    points++;
                }
            }
        }
        return points;
    }

    @Named("getReleaseYear")
    default Integer getReleaseYear(String releaseDate) {
        return Optional.ofNullable(releaseDate).filter(date -> date.length() == TEN)
                .map(date -> Integer.parseInt(date.substring(ZERO, FOUR)))
                .orElse(Year.now().getValue());
    }

    @Named("putType")
    default Type putType(int duration) {
        if (duration < SHORT_DURATION && duration != ZERO) {
            return Type.SHORTS;
        } else {
            return Type.MOVIE;
        }
    }

    @Named("getDurations")
    default Integer getDurations(List<Integer> episodeRunTime) {
        return episodeRunTime.stream()
                .findFirst()
                .orElse(DEFAULT_SERIAL_DURATION);
    }
}
