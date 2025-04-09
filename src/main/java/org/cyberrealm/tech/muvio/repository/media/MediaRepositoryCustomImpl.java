package org.cyberrealm.tech.muvio.repository.media;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.dto.MediaGalleryRequestDto;
import org.cyberrealm.tech.muvio.dto.MediaVibeRequestDto;
import org.cyberrealm.tech.muvio.model.Media;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MediaRepositoryCustomImpl implements MediaRepositoryCustom {
    private static final String SPLIT_PATTERN = "-";
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int NINE = 9;
    private static final String VIBES = "vibes";
    private static final String CATEGORIES = "categories";
    private static final String TITLE = "title";
    private static final String OPTIONS_I = "i";
    private static final String RELEASE_YEAR = "releaseYear";
    private static final String TYPE = "type";
    private final MongoTemplate mongoTemplate;

    @Override
    public List<Media> getAllMediaByVibes(MediaVibeRequestDto requestDto) {
        final Query query = new Query();
        addCriteriaYears(requestDto.years(), query);
        addCriteriaType(requestDto.type(), query);
        Optional.ofNullable(requestDto.categories())
                .filter(category -> !category.isEmpty())
                .ifPresent(category -> query.addCriteria(Criteria.where(CATEGORIES).in(category)));
        query.addCriteria(Criteria.where(VIBES).is(requestDto.vibe()));
        return mongoTemplate.find(query, Media.class);
    }

    @Override
    public List<Media> getAllForGallery(MediaGalleryRequestDto requestDto, Pageable pageable) {
        final Query query = new Query();
        Optional.ofNullable(requestDto.title()).filter(title -> !title.isEmpty())
                .ifPresent(
                        title -> query.addCriteria(Criteria.where(TITLE).regex(title, OPTIONS_I)));
        addCriteriaType(requestDto.type(), query);
        addCriteriaYears(requestDto.years(), query);
        query.with(pageable);
        return mongoTemplate.find(query, Media.class);
    }

    private void addCriteriaYears(String years, Query query) {
        Optional.ofNullable(years)
                .filter(year -> year.length() == NINE && year.contains(SPLIT_PATTERN))
                .map(year -> Arrays.stream(year.split(SPLIT_PATTERN))
                        .map(Integer::parseInt).toArray(Integer[]::new))
                .ifPresent(yearArray -> query.addCriteria(
                        Criteria.where(RELEASE_YEAR).gte(yearArray[ZERO]).lte(yearArray[ONE])));
    }

    private void addCriteriaType(String type, Query query) {
        Optional.ofNullable(type)
                .ifPresent(type1 -> query.addCriteria(Criteria.where(TYPE).is(type1)));
    }
}
