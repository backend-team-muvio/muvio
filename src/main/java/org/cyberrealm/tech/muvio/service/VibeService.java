package org.cyberrealm.tech.muvio.service;

import java.util.Set;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.cyberrealm.tech.muvio.model.Vibe;

public interface VibeService {
    Set<Vibe> getVibes(Set<String> ratings, Set<GenreEntity> genresMdb);
}
