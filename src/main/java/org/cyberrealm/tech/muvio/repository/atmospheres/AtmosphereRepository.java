package org.cyberrealm.tech.muvio.repository.atmospheres;

import org.cyberrealm.tech.muvio.model.Atmosphere;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtmosphereRepository extends MongoRepository<Atmosphere, Atmosphere.Vibe> {
}
