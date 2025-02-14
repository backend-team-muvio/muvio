package org.cyberrealm.tech.muvio.repository.genres;

import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends MongoRepository<GenreEntity, Integer> {
}
