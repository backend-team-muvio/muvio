package org.cyberrealm.tech.muvio.repository.movies;

import org.cyberrealm.tech.muvio.model.Movie;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MovieRepository extends MongoRepository<Movie, String> {
}
