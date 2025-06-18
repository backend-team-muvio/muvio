package org.cyberrealm.tech.muvio.repository;

import java.util.List;
import org.cyberrealm.tech.muvio.dto.PosterDto;
import org.cyberrealm.tech.muvio.model.LocalizationMedia;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocalizationMediaRepository extends MongoRepository<LocalizationMedia, String>,
        LocalizationMediaRepositoryCustom {

    @Aggregation(pipeline = {
            "{ '$match': { '_id': { '$regex': '^?1' } } }",
            "{ '$sample': { 'size': ?0 } }"
    })
    List<PosterDto> getRandomPosters(int size, String lang);
}
