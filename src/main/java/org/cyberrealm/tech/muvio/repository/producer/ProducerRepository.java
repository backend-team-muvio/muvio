package org.cyberrealm.tech.muvio.repository.producer;

import org.cyberrealm.tech.muvio.model.Producer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProducerRepository extends MongoRepository<Producer, String> {
}
