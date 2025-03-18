package org.cyberrealm.tech.muvio.repository.media;

import java.util.Optional;
import java.util.Set;
import org.cyberrealm.tech.muvio.dto.MovieBaseDto;
import org.cyberrealm.tech.muvio.dto.MovieBaseDtoWithPoints;
import org.cyberrealm.tech.muvio.dto.MovieDtoFromDb;
import org.cyberrealm.tech.muvio.dto.MovieDtoWithCastFromDb;
import org.cyberrealm.tech.muvio.dto.PosterDto;
import org.cyberrealm.tech.muvio.dto.TitleDto;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.model.Type;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaRepository extends MongoRepository<Media, String> {

    @Aggregation(pipeline = {
            "{ '$match': { 'releaseYear': { '$gte': ?0, '$lte': ?1 }, 'type': { $in: ?2 },"
                    + " 'vibes': ?3 } }",
            "{ '$unwind': { 'path': '$categories', 'preserveNullAndEmptyArrays': true } }",
            "{ '$group': { "
                    + "   '_id': '$_id', "
                    + "   'title': { '$first': '$title' }, "
                    + "   'releaseYear': { '$first': '$releaseYear' }, "
                    + "   'genres': { '$first': '$genres' }, "
                    + "   'rating': { '$first': '$rating' }, "
                    + "   'posterPath': { '$first': '$posterPath' }, "
                    + "   'duration': { '$first': '$duration' }, "
                    + "   'type': { '$first': '$type' }, "
                    + "   'points': { '$sum': { '$cond': [ { $or: [ { $eq: [?4, null] },"
                    + " { $eq: [?4, []] } ] }, 0, 1 ] } } } }",
            "{ '$match': { $or: [ { ?4: { $eq: null } }, { 'categories': { $in: ?4 } } ] } }"
    })
    Slice<MovieBaseDtoWithPoints> findAllByVibes(int startYear, int endYear, Set<String> type,
                                                 String vibe, Set<String> categories,
                                                 Pageable pageable);

    @Aggregation(pipeline = {
            "{ $match: { releaseYear: { $gte: ?0, $lte: ?1 } } }",
            "{ $match: { title: { $regex: ?2, $options: 'i' } } }",
            "{ $match: { type: { $in: ?3 } } }"
    })
    Slice<MovieBaseDto> getAllForGallery(int startYear, int endYear, String title, Set<String> type,
                                         Pageable pageable);

    @Aggregation(pipeline = {
            "{ '$sample': { 'size': ?0 } }"
    })
    Set<MovieBaseDto> getAllLuck(int size);

    @Aggregation(pipeline = {
            "{ '$match': { 'type': ?0, 'genres': ?1, 'releaseYear': { $gte: ?2 } } }",
            "{ '$sort': { 'rating': -1 } }"
    })
    Slice<MovieBaseDto> findMoviesByTypeGenreAndYears(Type type, String genre, int minYear,
                                                      Pageable pageable);

    Optional<MovieDtoFromDb> findMovieById(String id);

    Slice<MovieDtoWithCastFromDb> findByTopListsContaining(String topList, Pageable pageable);

    @Query(value = "{ 'posterPath': { '$ne': null } }", fields = "{ 'id': 1, 'posterPath': 1 }")
    Slice<PosterDto> findAllPosters(Pageable pageable);

    @Query(value = "{}", fields = "{ 'title': 1 }")
    Slice<TitleDto> findAllTitles(Pageable pageable);

    MovieDtoFromDb findByTitle(String title);
}
