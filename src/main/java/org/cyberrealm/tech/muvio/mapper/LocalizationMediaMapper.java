package org.cyberrealm.tech.muvio.mapper;

import static org.cyberrealm.tech.muvio.common.Constants.FULL_DURATION_PATTERN;
import static org.cyberrealm.tech.muvio.common.Constants.MINUTES_ONLY_PATTERN;
import static org.cyberrealm.tech.muvio.common.Constants.ONE;
import static org.cyberrealm.tech.muvio.common.Constants.SHORT_DURATION;
import static org.cyberrealm.tech.muvio.common.Constants.ZERO;

import info.movito.themoviedbapi.model.core.ProductionCountry;
import info.movito.themoviedbapi.model.movies.MovieDb;
import info.movito.themoviedbapi.model.tv.series.TvSeriesDb;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.cyberrealm.tech.muvio.config.MapperConfig;
import org.cyberrealm.tech.muvio.dto.MediaBaseDto;
import org.cyberrealm.tech.muvio.dto.MediaDto;
import org.cyberrealm.tech.muvio.dto.MediaDtoFromDb;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCast;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithCastFromDb;
import org.cyberrealm.tech.muvio.dto.MediaDtoWithPoints;
import org.cyberrealm.tech.muvio.model.LocalizationEntry;
import org.cyberrealm.tech.muvio.model.LocalizationMedia;
import org.cyberrealm.tech.muvio.model.Media;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class,
        uses = {GenreMapper.class, MediaMapper.class, ActorMapper.class})
public interface LocalizationMediaMapper {
    @Mapping(target = "id", expression = "java(createId(String.valueOf(movieDb.getId()),"
            + " localizationEntry.getLang()))")
    @Mapping(target = "duration", expression = "java(updateDuration("
            + "movieDb.getRuntime(), localizationEntry.getTimeH(), localizationEntry.getTimeM()))")
    @Mapping(target = "genres", expression = "java(org.cyberrealm.tech.muvio.mapper.GenreMapper"
            + ".splitAndCleanGenres(movieDb.getGenres(), localizationEntry.getAmpersand()))")
    @Mapping(target = "posterPath", expression = "java(org.cyberrealm.tech.muvio.common.Constants"
            + ".IMAGE_PATH_W200 + movieDb.getPosterPath())")
    @Mapping(target = "type", expression = "java(putLocalizationType(movieDb.getRuntime(),"
            + " localizationEntry))")
    @Mapping(target = "countries", expression = "java(getLocalizedCountryName("
            + "movieDb.getProductionCountries(), localizationEntry.getLang()))")
    @Mapping(target = "title", source = "movieDb.title")
    LocalizationMedia toLocalizationMovie(MovieDb movieDb, LocalizationEntry localizationEntry);

    @Mapping(target = "title", source = "tvSeriesDb.name")
    @Mapping(target = "id", expression = "java(createId("
            + "org.cyberrealm.tech.muvio.common.Constants.TV + "
            + "tvSeriesDb.getId(), localizationEntry.getLang()))")
    @Mapping(target = "duration", expression = "java(updateDuration(tvSeriesDb.getEpisodeRunTime()"
            + ".stream().findFirst().orElse(org.cyberrealm.tech.muvio.common.Constants"
            + ".DEFAULT_SERIAL_DURATION), localizationEntry.getTimeH(),"
            + " localizationEntry.getTimeM()))")
    @Mapping(target = "genres", expression = "java(org.cyberrealm.tech.muvio.mapper.GenreMapper"
            + ".splitAndCleanGenres(tvSeriesDb.getGenres(), localizationEntry.getAmpersand()))")
    @Mapping(target = "posterPath", expression = "java(org.cyberrealm.tech.muvio.common.Constants"
            + ".IMAGE_PATH_W200 + tvSeriesDb.getPosterPath())")
    @Mapping(target = "type", source = "localizationEntry.tvShows")
    @Mapping(target = "countries", expression = "java(getLocalizedCountryName("
            + "tvSeriesDb.getProductionCountries(), localizationEntry.getLang()))")
    LocalizationMedia toLocalizationTvSerial(
            TvSeriesDb tvSeriesDb, LocalizationEntry localizationEntry);

    @Mapping(target = "id", source = "localizationMedia.id")
    @Mapping(target = "title", source = "localizationMedia.title")
    @Mapping(target = "genres", source = "localizationMedia.genres")
    @Mapping(target = "posterPath", source = "localizationMedia.posterPath")
    @Mapping(target = "duration", source = "localizationMedia.duration")
    @Mapping(target = "type", source = "localizationMedia.type")
    MediaBaseDto toMediaBaseDto(Media media, LocalizationMedia localizationMedia);

    @Mapping(target = "id", source = "localizationMedia.id")
    @Mapping(target = "title", source = "localizationMedia.title")
    @Mapping(target = "actors", source = "media.actors", qualifiedByName = "toActorDto")
    @Mapping(target = "duration", source = "localizationMedia.duration")
    @Mapping(target = "points", expression = "java(org.cyberrealm.tech.muvio.util"
            + ".MediaPointsUtil.calculatePoints(media, categories))")
    @Mapping(target = "genres", source = "localizationMedia.genres")
    @Mapping(target = "type", source = "localizationMedia.type")
    @Mapping(target = "posterPath", source = "localizationMedia.posterPath",
            qualifiedByName = "changePoster")
    @Mapping(target = "trailer", expression = "java(resolveTrailer(media.getTrailer(),"
            + " localizationMedia))")
    @Mapping(target = "countries", source = "localizationMedia.countries")
    @Mapping(target = "overview", source = "localizationMedia.overview")
    MediaDtoWithPoints toMediaDtoWithPoints(Media media, Set<String> categories,
                                            LocalizationMedia localizationMedia);

    @Mapping(target = "id", source = "localizationMedia.id")
    @Mapping(target = "title", source = "localizationMedia.title")
    @Mapping(target = "genres", source = "localizationMedia.genres")
    @Mapping(target = "posterPath", source = "localizationMedia.posterPath")
    @Mapping(target = "duration", source = "localizationMedia.duration")
    @Mapping(target = "type", source = "localizationMedia.type")
    MediaBaseDto toLocalizateMediaBaseDto(LocalizationMedia localizationMedia, MediaBaseDto media);

    @Mapping(target = "id", source = "localizationMedia.id")
    @Mapping(target = "title", source = "localizationMedia.title")
    @Mapping(target = "genres", source = "localizationMedia.genres")
    @Mapping(target = "posterPath", source = "localizationMedia.posterPath",
            qualifiedByName = "changePoster")
    @Mapping(target = "duration", source = "localizationMedia.duration")
    @Mapping(target = "type", source = "localizationMedia.type")
    @Mapping(target = "trailer", expression = "java(resolveTrailer(media.getTrailer(),"
            + " localizationMedia))")
    @Mapping(target = "countries", source = "localizationMedia.countries")
    @Mapping(target = "overview", source = "localizationMedia.overview")
    @Mapping(target = "actors", source = "media.actors", qualifiedByName = "toActorDto")
    MediaDto toMediaDto(LocalizationMedia localizationMedia, Media media);

    @Mapping(target = "id", source = "localizationMedia.id")
    @Mapping(target = "title", source = "localizationMedia.title")
    @Mapping(target = "genres", source = "localizationMedia.genres")
    @Mapping(target = "posterPath", source = "localizationMedia.posterPath",
            qualifiedByName = "changePoster")
    @Mapping(target = "duration", source = "localizationMedia.duration")
    @Mapping(target = "type", source = "localizationMedia.type")
    @Mapping(target = "trailer", expression = "java(resolveTrailer(mediaDtoFromDb.trailer(),"
            + " localizationMedia))")
    @Mapping(target = "countries", source = "localizationMedia.countries")
    @Mapping(target = "overview", source = "localizationMedia.overview")
    @Mapping(target = "actors", source = "mediaDtoFromDb.actors", qualifiedByName = "toActorDto")
    MediaDto toMediaDtoFromDto(LocalizationMedia localizationMedia, MediaDtoFromDb mediaDtoFromDb);

    @Mapping(target = "id", source = "localizationMedia.id")
    @Mapping(target = "title", source = "localizationMedia.title")
    @Mapping(target = "genres", source = "localizationMedia.genres")
    @Mapping(target = "posterPath", source = "localizationMedia.posterPath")
    @Mapping(target = "duration", source = "localizationMedia.duration")
    @Mapping(source = "mediaDtoFromDb.actors", target = "actors", qualifiedByName = "toListActors")
    MediaDtoWithCast toMediaDtoWithCast(LocalizationMedia localizationMedia,
                                        MediaDtoWithCastFromDb mediaDtoFromDb);

    @Named("resolveTrailer")
    default String resolveTrailer(String mediaTrailer, LocalizationMedia localizationMedia) {
        return localizationMedia.getTrailer() != null ? localizationMedia.getTrailer()
                : mediaTrailer;
    }

    default List<String> getLocalizedCountryName(List<ProductionCountry> productionCountries,
                                                 String lang) {
        return productionCountries.stream().map(productionCountry -> {
            final String isoCode = productionCountry.getIsoCode();
            Locale countryLocale = new Locale.Builder().setRegion(isoCode).build();
            Locale displayLocale = Locale.forLanguageTag(lang);
            return countryLocale.getDisplayCountry(displayLocale);
        }).limit(ONE).toList();
    }

    default String putLocalizationType(int duration, LocalizationEntry localizationEntry) {
        if (duration < SHORT_DURATION && duration != ZERO) {
            return localizationEntry.getShorts();
        } else {
            return localizationEntry.getMovie();
        }
    }

    default String createId(String id, String land) {
        return land + id;
    }

    default String updateDuration(Integer durationInMinutes, String hourSuffix,
                                  String minuteSuffix) {
        long hours = TimeUnit.MINUTES.toHours(durationInMinutes);
        long minutes = durationInMinutes - TimeUnit.HOURS.toMinutes(hours);
        return hours > ZERO ? String.format(
                FULL_DURATION_PATTERN, hours, hourSuffix, minutes, minuteSuffix)
                : String.format(MINUTES_ONLY_PATTERN, minutes, minuteSuffix);
    }
}
