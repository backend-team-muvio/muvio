package org.cyberrealm.tech.muvio.repository.impl;

import static org.cyberrealm.tech.muvio.common.Constants.ONE;
import static org.cyberrealm.tech.muvio.common.Constants.REGEX_ANCHOR_START;
import static org.cyberrealm.tech.muvio.common.Constants.REGEX_ANY;
import static org.cyberrealm.tech.muvio.common.Constants.REGEX_FLAG_IGNORE_CASE;
import static org.cyberrealm.tech.muvio.common.Constants.TITLE;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.dto.TitleDto;
import org.cyberrealm.tech.muvio.model.LocalizationMedia;
import org.cyberrealm.tech.muvio.repository.LocalizationMediaRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LocalizationMediaRepositoryCustomImpl implements LocalizationMediaRepositoryCustom {
    private static final String _ID = "_id";
    private static final String ID = "id";
    private static final String LOCALIZATION_MEDIA = "localization_media";
    private final MongoTemplate mongoTemplate;

    @Override
    public List<LocalizationMedia> getAllForGalleryLocalization(
            List<String> mediaIds, String partTitle) {
        if (mediaIds.isEmpty()) {
            return List.of();
        }
        final Query query = buildQueryByPartialTitle(partTitle);
        query.addCriteria(Criteria.where(_ID).in(mediaIds));
        return mongoTemplate.find(query, LocalizationMedia.class);
    }

    @Override
    public Slice<TitleDto> findAllTitles(Pageable pageable, String lang) {
        final List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where(_ID).regex(REGEX_ANCHOR_START + lang)));
        if (pageable.getSort().isSorted()) {
            operations.add(Aggregation.sort(pageable.getSort()));
        }
        operations.add(Aggregation.skip((long) pageable.getOffset()));
        operations.add(Aggregation.limit(pageable.getPageSize() + ONE));
        operations.add(Aggregation.project(TITLE).andExpression(_ID).as(ID));
        final List<TitleDto> mapped = mongoTemplate.aggregate(Aggregation.newAggregation(
                operations), LOCALIZATION_MEDIA, TitleDto.class).getMappedResults();
        final boolean hasNext = mapped.size() > pageable.getPageSize();
        if (hasNext) {
            mapped.removeLast();
        }
        return new SliceImpl<>(mapped, pageable, hasNext);
    }

    @Override
    public List<LocalizationMedia> findByTitle(String title, Pageable pageable, String lang) {
        Query query = buildQueryByPartialTitle(title);
        query.addCriteria(Criteria.where(_ID).regex(REGEX_ANCHOR_START + lang));
        query.with(pageable);
        return mongoTemplate.find(query, LocalizationMedia.class);
    }

    private Query buildQueryByPartialTitle(String partTitle) {
        final Query query = new Query();
        Optional.ofNullable(partTitle)
                .map(String::trim)
                .filter(title -> !title.isEmpty())
                .ifPresent(
                        title -> {
                            String regexPattern = REGEX_ANY + Pattern.quote(title) + REGEX_ANY;
                            query.addCriteria(Criteria.where(TITLE).regex(regexPattern,
                                    REGEX_FLAG_IGNORE_CASE));
                        });
        return query;
    }
}
