package org.cyberrealm.tech.muvio.mapper;

import static org.cyberrealm.tech.muvio.common.Constants.DEFAULT_SERIAL_DURATION;
import static org.cyberrealm.tech.muvio.common.Constants.FOUR;
import static org.cyberrealm.tech.muvio.common.Constants.SHORT_DURATION;
import static org.cyberrealm.tech.muvio.common.Constants.TEN;
import static org.cyberrealm.tech.muvio.common.Constants.ZERO;

import info.movito.themoviedbapi.model.core.ProductionCountry;
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

    @Mapping(source = "actors", target = "actors", qualifiedByName = "toActorDto")
    @Mapping(source = "duration", target = "duration", qualifiedByName = "toDuration")
    MediaDto toMovieDto(MediaDtoFromDb movie);

    @Mapping(target = "id", expression = "java(String.valueOf(movieDb.getId()))")
    @Mapping(source = "movieDb.runtime", target = "duration")
    @Mapping(source = "movieDb.voteAverage", target = "rating")
    @Mapping(source = "genres", target = "genres", qualifiedByName = "toGenreEntity")
    @Mapping(target = "posterPath", expression = "java(org.cyberrealm.tech.muvio.common.Constants"
            + ".IMAGE_PATH + movieDb.getPosterPath())")
    @Mapping(source = "releaseDate", target = "releaseYear", qualifiedByName = "getReleaseYear")
    @Mapping(source = "movieDb.runtime", target = "type", qualifiedByName = "putType")
    @Mapping(source = "movieDb.productionCountries", target = "countries",
            qualifiedByName = "putCountries")
    Media toEntity(MovieDb movieDb);

    @Mapping(source = "tvSeriesDb.name", target = "title")
    @Mapping(source = "tvSeriesDb.voteAverage", target = "rating")
    @Mapping(target = "type", expression = "java(org.cyberrealm.tech.muvio.model.Type.TV_SHOW)")
    @Mapping(target = "id", expression =
            "java(org.cyberrealm.tech.muvio.common.Constants.TV + tvSeriesDb.getId())")
    @Mapping(source = "genres", target = "genres", qualifiedByName = "toGenreEntity")
    @Mapping(source = "episodeRunTime", target = "duration", qualifiedByName = "getDurations")
    @Mapping(target = "posterPath", expression = "java(org.cyberrealm.tech.muvio.common.Constants"
            + ".IMAGE_PATH + tvSeriesDb.getPosterPath())")
    @Mapping(source = "firstAirDate", target = "releaseYear", qualifiedByName = "getReleaseYear")
    @Mapping(source = "tvSeriesDb.productionCountries", target = "countries",
            qualifiedByName = "putCountries")
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
        return hours > ZERO ? String.format("%dh %02dm", hours, minutes)
                : String.format("%02dm", minutes);
    }

    default int calculatePoints(Media media, Set<String> categories) {
        int points = ZERO;
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

    @Named("putCountries")
    default List<String> putCountries(List<ProductionCountry> productionCountries) {
        return productionCountries.stream().map(ProductionCountry::getName).toList();
    }
}
