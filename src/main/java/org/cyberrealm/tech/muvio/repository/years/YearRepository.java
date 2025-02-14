package org.cyberrealm.tech.muvio.repository.years;

import org.cyberrealm.tech.muvio.model.Year;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YearRepository extends MongoRepository<Year, Integer> {
}
