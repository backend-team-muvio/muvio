package org.cyberrealm.tech.muvio.repository.durations;

import org.cyberrealm.tech.muvio.model.Duration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DurationRepository extends MongoRepository<Duration, Integer> {
}
