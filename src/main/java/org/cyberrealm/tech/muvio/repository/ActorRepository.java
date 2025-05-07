package org.cyberrealm.tech.muvio.repository;

import org.cyberrealm.tech.muvio.model.Actor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActorRepository extends MongoRepository<Actor, String> {
}
