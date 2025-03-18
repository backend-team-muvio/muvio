package org.cyberrealm.tech.muvio.controller;

import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.dto.MediaDto;
import org.cyberrealm.tech.muvio.dto.MovieBaseDto;
import org.cyberrealm.tech.muvio.dto.MovieBaseDtoWithPoints;
import org.cyberrealm.tech.muvio.dto.MovieDtoWithCast;
import org.cyberrealm.tech.muvio.dto.MovieGalleryRequestDto;
import org.cyberrealm.tech.muvio.dto.MovieVibeRequestDto;
import org.cyberrealm.tech.muvio.dto.PosterDto;
import org.cyberrealm.tech.muvio.dto.TitleDto;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.service.MediaService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {
    private final MediaService mediaService;

    @GetMapping("/{id}")
    public MediaDto getMovieById(@PathVariable String id) {
        return mediaService.getMovieById(id);
    }

    @PostMapping
    public Media saveMovie(@RequestBody Media media) {
        return mediaService.saveMovie(media);
    }

    @DeleteMapping("/{id}")
    public void deleteMovieById(@PathVariable String id) {
        mediaService.deleteMovieById(id);
    }

    @PutMapping("/{id}")
    public Media updateMovie(@PathVariable String id, @RequestBody Media media) {
        return mediaService.updateMovie(id, media);
    }

    @GetMapping("/vibe")
    public Slice<MovieBaseDtoWithPoints> getAllMoviesByVibe(
            @RequestBody @Valid MovieVibeRequestDto requestDto, Pageable pageable) {
        return mediaService.getAllMoviesByVibe(requestDto, pageable);
    }

    @GetMapping("/gallery")
    public Slice<MovieBaseDto> getAllForGallery(@RequestBody MovieGalleryRequestDto requestDto,
                                                Pageable pageable) {
        return mediaService.getAllForGallery(requestDto, pageable);
    }

    @GetMapping("/luck/{size}")
    public Set<MovieBaseDto> getAllLuck(@PathVariable int size) {
        return mediaService.getAllLuck(size);
    }

    @GetMapping("/recommendations")
    public Slice<MovieBaseDto> getRecommendations(Pageable pageable) {
        return mediaService.getRecommendations(pageable);
    }

    @GetMapping("/top-list/{topList}")
    public Slice<MovieDtoWithCast> getMoviesByTopList(@PathVariable String topList,
                                                      Pageable pageable) {
        return mediaService.findMoviesByTopLists(topList, pageable);
    }

    @GetMapping("/posters")
    public Slice<PosterDto> findAllPosters(Pageable pageable) {
        return mediaService.findAllPosters(pageable);
    }

    @GetMapping("/titles")
    public Slice<TitleDto> findAllTitle(Pageable pageable) {
        return mediaService.findAllTitles(pageable);
    }

    @GetMapping("/titles/{title}")
    public MediaDto findByTitle(@PathVariable String title) {
        return mediaService.findByTitle(title);
    }
}
