package org.cyberrealm.tech.muvio.repository;

import org.cyberrealm.tech.muvio.model.LocalizationMedia;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocalizationMediaRepository extends MongoRepository<LocalizationMedia, String> {
}
