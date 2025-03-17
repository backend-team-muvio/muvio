package org.cyberrealm.tech.muvio.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.dto.MediaDto;
import org.cyberrealm.tech.muvio.exception.EntityNotFoundException;
import org.cyberrealm.tech.muvio.mapper.MediaMapper;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.repository.media.MediaRepository;
import org.cyberrealm.tech.muvio.service.MediaService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {
    private final MediaRepository mediaRepository;
    private final MediaMapper movieMapper;

    @Override
    public List<MediaDto> getAllMovies(Pageable pageable) {
        return mediaRepository.findAll(pageable).stream().map(movieMapper::toMovieDto).toList();
    }

    @Override
    public Media getMovieById(String id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no movie with this id: "
                        + id));
    }

    @Override
    public Media saveMovie(Media media) {
        return mediaRepository.save(media);
    }

    @Override
    public void deleteMovieById(String id) {
        mediaRepository.deleteById(id);
    }

    @Override
    public Media updateMovie(String id, Media updatedMedia) {
        final Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no movie with this id: "
                        + id));
        media.setTitle(updatedMedia.getTitle());
        media.setGenres(updatedMedia.getGenres());
        media.setRating(updatedMedia.getRating());
        media.setTrailer(updatedMedia.getTrailer());
        return mediaRepository.save(media);
    }
}
