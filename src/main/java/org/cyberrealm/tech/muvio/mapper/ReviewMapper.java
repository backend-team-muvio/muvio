package org.cyberrealm.tech.muvio.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.cyberrealm.tech.muvio.config.MapperConfig;
import org.cyberrealm.tech.muvio.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface ReviewMapper {
    @Mapping(source = "authorDetails.avatarPath", target = "avatarPath")
    @Mapping(source = "createdAt", target = "time", qualifiedByName = "mapToTime")
    @Mapping(source = "authorDetails.rating", target = "rating", qualifiedByName = "getRating")
    Review toEntity(info.movito.themoviedbapi.model.core.Review review);

    @Named("mapToTime")
    default LocalDateTime mapToTime(String createdAt) {
        Instant instant = Instant.parse(createdAt);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    @Named("getRating")
    default Integer getRating(Double voteAverage) {
        return voteAverage.intValue();
    }
}
