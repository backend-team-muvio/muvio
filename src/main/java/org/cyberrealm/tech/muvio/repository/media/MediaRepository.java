package org.cyberrealm.tech.muvio.repository.media;

import org.cyberrealm.tech.muvio.model.Media;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaRepository extends MongoRepository<Media, String> {
}
