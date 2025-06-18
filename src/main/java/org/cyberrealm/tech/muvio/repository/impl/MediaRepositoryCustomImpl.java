package org.cyberrealm.tech.muvio.repository.impl;

import static org.cyberrealm.tech.muvio.common.Constants.LANGUAGE_EN;
import static org.cyberrealm.tech.muvio.common.Constants.ONE;
import static org.cyberrealm.tech.muvio.common.Constants.REGEX_ANCHOR_START;
import static org.cyberrealm.tech.muvio.common.Constants.REGEX_ANY;
import static org.cyberrealm.tech.muvio.common.Constants.REGEX_FLAG_IGNORE_CASE;
import static org.cyberrealm.tech.muvio.common.Constants.TITLE;
import static org.cyberrealm.tech.muvio.common.Constants.ZERO;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.dto.MediaGalleryRequestDto;
import org.cyberrealm.tech.muvio.dto.MediaVibeRequestDto;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.model.Type;
import org.cyberrealm.tech.muvio.repository.MediaRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MediaRepositoryCustomImpl implements MediaRepositoryCustom {
    private static final String SPLIT_PATTERN = "-";
    private static final String VIBES = "vibes";
    private static final String CATEGORIES = "categories";
    private static final String RELEASE_YEAR = "releaseYear";
    private static final String TYPE = "type";
    private static final String REGEX_ANCHOR_END = "$";
    private static final String YEAR_RANGE_REGEX = "\\d{4}-\\d{4}";
    private final MongoTemplate mongoTemplate;

    @Override
    public List<Media> getAllMediaByVibes(MediaVibeRequestDto requestDto) {
        final Query query = new Query();
        addCriteriaYears(requestDto.years(), query);
        addCriteriaType(requestDto.type(), query);
        Optional.ofNullable(requestDto.categories())
                .filter(category -> !category.isEmpty())
                .ifPresent(category ->
                        query.addCriteria(Criteria.where(CATEGORIES).in(getCategories(category))));
        query.addCriteria(Criteria.where(VIBES).is(requestDto.vibe().toUpperCase()));
        return mongoTemplate.find(query, Media.class);
    }

    @Override
    public List<Media> getAllForGallery(MediaGalleryRequestDto requestDto, Pageable pageable) {
        final Query query = new Query();
        final String lang = requestDto.lang();
        if (lang == null || lang.isEmpty() || lang.equals(LANGUAGE_EN)) {
            Optional.ofNullable(requestDto.title())
                    .map(String::trim)
                    .filter(title -> !title.isEmpty())
                    .ifPresent(
                            title -> {
                                String regexPattern = REGEX_ANY + Pattern.quote(title) + REGEX_ANY;
                                query.addCriteria(Criteria.where(TITLE).regex(regexPattern,
                                        REGEX_FLAG_IGNORE_CASE));
                            });
            query.with(pageable);
        }
        addCriteriaType(requestDto.type(), query);
        addCriteriaYears(requestDto.years(), query);
        return mongoTemplate.find(query, Media.class);
    }

    private void addCriteriaYears(String years, Query query) {
        Optional.ofNullable(years)
                .map(String::trim)
                .filter(year -> year.matches(YEAR_RANGE_REGEX))
                .map(year -> Arrays.stream(year.split(SPLIT_PATTERN))
                        .map(Integer::parseInt)
                        .toArray(Integer[]::new))
                .ifPresent(yearArray -> query.addCriteria(
                        Criteria.where(RELEASE_YEAR)
                                .gte(yearArray[ZERO])
                                .lte(yearArray[ONE])));
    }

    private void addCriteriaType(String type, Query query) {
        Optional.ofNullable(type)
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .filter(item -> Arrays.stream(Type.values())
                        .anyMatch(element -> element.name().equalsIgnoreCase(item)))
                .ifPresent(validType -> {
                    String regexPattern = REGEX_ANCHOR_START + Pattern.quote(validType)
                            + REGEX_ANCHOR_END;
                    query.addCriteria(Criteria.where(TYPE).regex(regexPattern,
                            REGEX_FLAG_IGNORE_CASE));
                });
    }

    private Set<String> getCategories(Set<String> categories) {
        return categories != null
                ? categories.stream().map(String::toUpperCase).collect(Collectors.toSet())
                : Set.of();
    }
}
