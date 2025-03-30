package org.cyberrealm.tech.muvio.service;

import java.util.Set;

public interface AwardService {
    Set<String> getImdbTop250Movies();

    Set<String> getImdbTop250TvShows();

    Set<String> getOscarWinningMovies();

    Set<String> getEmmyWinningTvShows();
}
