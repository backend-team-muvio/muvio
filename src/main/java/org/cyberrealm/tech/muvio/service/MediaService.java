package org.cyberrealm.tech.muvio.service;

import java.util.List;
import org.cyberrealm.tech.muvio.dto.MediaDto;
import org.cyberrealm.tech.muvio.model.Media;
import org.springframework.data.domain.Pageable;

public interface MediaService {

    List<MediaDto> getAllMovies(Pageable pageable);

    Media getMovieById(String id);

    Media saveMovie(Media media);

    void deleteMovieById(String id);

    Media updateMovie(String id, Media updatedMedia);
}
