package org.cyberrealm.tech.muvio.service.impl;

import info.movito.themoviedbapi.model.movies.KeywordResults;
import info.movito.themoviedbapi.model.movies.ReleaseDate;
import info.movito.themoviedbapi.model.movies.ReleaseInfo;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.model.Atmosphere;
import org.cyberrealm.tech.muvio.model.Movie;
import org.cyberrealm.tech.muvio.repository.atmospheres.AtmosphereRepository;
import org.cyberrealm.tech.muvio.service.AtmosphereService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AtmosphereServiceImpl implements AtmosphereService {
    private static final Set<String> CHILD_FRIENDLY_RATINGS = Set.of("G", "PG", "U");
    private final AtmosphereRepository atmosphereRepository;
    @Override
    public void putAtmosphere(List<ReleaseInfo> releaseInfo, Movie movieMdb) {
        if (isSuitableForChildren(releaseInfo)) {
            movieMdb.getAtmospheres().add(atmosphereRepository.findById(Atmosphere.Vibe.MAKE_ME_CHILD).orElseThrow());
            System.out.println("genre   " + movieMdb.getGenres().toString()  + " name " + movieMdb.getName());
        }
    }

    private boolean isSuitableForChildren(List<ReleaseInfo> releaseInfo) {
        for (ReleaseInfo release : releaseInfo) {
            for (ReleaseDate dates : release.getReleaseDates()) {
                if (CHILD_FRIENDLY_RATINGS.contains(dates.getCertification())) {
                    return true;
                }
            }
        }
        return false;
    }
}
