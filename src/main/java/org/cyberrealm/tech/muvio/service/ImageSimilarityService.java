package org.cyberrealm.tech.muvio.service;

import dev.brachtendorf.jimagehash.hash.Hash;
import java.util.Set;

public interface ImageSimilarityService {
    void addIfUniqueHash(String imageUrl, Set<Hash> seenHashes, Set<String> uniqueFilePaths);
}
