package org.cyberrealm.tech.muvio.repository.movies;

import org.cyberrealm.tech.muvio.model.Movie;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {
}
