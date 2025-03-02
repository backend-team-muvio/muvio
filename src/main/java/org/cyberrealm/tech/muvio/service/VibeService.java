package org.cyberrealm.tech.muvio.service;

import info.movito.themoviedbapi.model.movies.ReleaseInfo;
import java.util.List;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.cyberrealm.tech.muvio.model.Vibe;

public interface VibeService {
    Set<Vibe> getVibes(List<ReleaseInfo> releaseInfo, Set<GenreEntity> genresMdb);
}
