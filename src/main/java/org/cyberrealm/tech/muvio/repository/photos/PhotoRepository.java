package org.cyberrealm.tech.muvio.repository.photos;

import org.cyberrealm.tech.muvio.model.Photo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoRepository extends MongoRepository<Photo, String> {
}
