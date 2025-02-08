package org.cyberrealm.tech.muvio.repository.actors;

import org.cyberrealm.tech.muvio.model.Actor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActorRepository extends MongoRepository<Actor, String> {
}
