package org.cyberrealm.tech.muvio.service;

public interface MovieSyncService {
    void loadGenres(String language);

    void importMovies(int fromPage, int toPage, String language, String location);

    void deleteAll();
}
