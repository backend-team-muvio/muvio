package org.cyberrealm.tech.muvio.mapper;

import info.movito.themoviedbapi.model.core.ProductionCountry;
import info.movito.themoviedbapi.model.movies.MovieDb;
import org.cyberrealm.tech.muvio.config.MapperConfig;
import org.cyberrealm.tech.muvio.model.LocalizationEntry;
import org.cyberrealm.tech.muvio.model.LocalizationMedia;
import org.cyberrealm.tech.muvio.model.Type;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.cyberrealm.tech.muvio.common.Constants.FULL_DURATION_PATTERN;
import static org.cyberrealm.tech.muvio.common.Constants.MINUTES_ONLY_PATTERN;
import static org.cyberrealm.tech.muvio.common.Constants.SHORT_DURATION;
import static org.cyberrealm.tech.muvio.common.Constants.ZERO;

@Mapper(config = MapperConfig.class, uses = {GenreMapper.class, MediaMapper.class})
public interface LocalizationMediaMapper {
    @Mapping(target = "id", expression = "java(createId(movieDb.getId(), localizationEntry.getLang()))")
    @Mapping(target = "duration", expression = "java(updateDuration("
            + "movieDb.getRuntime(), localizationEntry.getTimeH(), localizationEntry.getTimeM()))")
    @Mapping(source = "movieDb.genres", target = "genres", qualifiedByName = "toSetStringGenres")
    @Mapping(target = "posterPath", expression = "java(org.cyberrealm.tech.muvio.common.Constants"
            + ".IMAGE_PATH_W200 + movieDb.getPosterPath())")
    @Mapping(target = "type", expression = "java(putLocalizationType(movieDb.getRuntime(), localizationEntry))")
    @Mapping(target = "countries", expression = "java(getLocalizedCountryName("
            + "movieDb.getProductionCountries(), localizationEntry.getLang()))")
    LocalizationMedia toLocalizationMedia(MovieDb movieDb, LocalizationEntry localizationEntry);

    default List<String> getLocalizedCountryName(List<ProductionCountry> productionCountries, String lang) {
        return productionCountries.stream().map(productionCountry -> {
            final String isoCode = productionCountry.getIsoCode();
            Locale countryLocale = new Locale.Builder().setRegion(isoCode).build();
            Locale displayLocale = Locale.forLanguageTag(lang);
            return countryLocale.getDisplayCountry(displayLocale);
        }).toList();
    }

    default String putLocalizationType(int duration, LocalizationEntry localizationEntry) {
        if (duration < SHORT_DURATION && duration != ZERO) {
            return localizationEntry.getShorts();
        } else {
            return localizationEntry.getMovie();
        }
    }

    default String createId(int id, String land) {
        return land + id;
    }

    default String updateDuration(Integer durationInMinutes, String hourSuffix, String minuteSuffix) {
        long hours = TimeUnit.MINUTES.toHours(durationInMinutes);
        long minutes = durationInMinutes - TimeUnit.HOURS.toMinutes(hours);
        return hours > ZERO ? String.format(
                FULL_DURATION_PATTERN, hours, hourSuffix, minutes, minuteSuffix)
                : String.format(MINUTES_ONLY_PATTERN, minutes, minuteSuffix);
    }
}
