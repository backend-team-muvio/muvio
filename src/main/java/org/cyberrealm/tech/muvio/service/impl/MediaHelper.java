package org.cyberrealm.tech.muvio.service.impl;

import info.movito.themoviedbapi.model.core.image.Artwork;
import info.movito.themoviedbapi.model.core.video.VideoResults;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MediaHelper {
    public Set<String> formatPhotos(List<Artwork> artworks) {
        return artworks.stream()
                .map(artwork -> "https://image.tmdb.org/t/p/original" + artwork.getFilePath())
                .collect(Collectors.toSet());
    }

    public String extractTrailer(VideoResults videos) {
        return videos.getResults().stream()
                .filter(video -> video.getType().equals("Trailer"))
                .map(video -> "https://www.youtube.com/watch?v=" + video.getKey())
                .findFirst().orElse(null);
    }
}
