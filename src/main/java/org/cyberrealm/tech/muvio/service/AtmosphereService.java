package org.cyberrealm.tech.muvio.service;

import info.movito.themoviedbapi.model.movies.ReleaseInfo;
import org.cyberrealm.tech.muvio.model.Movie;

import java.util.List;

public interface AtmosphereService {
    void putAtmosphere(List<ReleaseInfo> releaseInfo, Movie movieMdb);
}
