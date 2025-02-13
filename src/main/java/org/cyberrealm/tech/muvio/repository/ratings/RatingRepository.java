package org.cyberrealm.tech.muvio.repository.ratings;

import org.cyberrealm.tech.muvio.model.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends MongoRepository<Rating, Double> {
}
