package org.cyberrealm.tech.muvio.service;

import org.cyberrealm.tech.muvio.model.Type;

public interface MediaSyncService {

    void importMedia(Type type, int fromPage, int toPage, String language, String location);

    void deleteAll();
}
