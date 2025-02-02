package org.cyberrealm.tech.muvio.repository.genres;

import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GenreRepository extends MongoRepository<GenreEntity, String> {

}
