package org.cyberrealm.tech.muvio.service;

import java.util.Set;
import org.cyberrealm.tech.muvio.model.Type;

public interface MediaSyncService {

    void importMedia(Type type, int fromPage, int toPage, String language, String location,
                     Set<String> imdbTop250, Set<String> oscarWinningMovie);

    void deleteAll();
}
