package org.cyberrealm.tech.muvio.service;

public interface TmdbService {
    void importMovies(int fromPage, int toPage, String language, String location);

    void deleteAll();
}
