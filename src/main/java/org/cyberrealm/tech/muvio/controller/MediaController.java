package org.cyberrealm.tech.muvio.controller;

import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.dto.MovieBaseDto;
import org.cyberrealm.tech.muvio.dto.MovieBaseDtoWithPoints;
import org.cyberrealm.tech.muvio.dto.MovieDto;
import org.cyberrealm.tech.muvio.dto.MovieDtoWithCast;
import org.cyberrealm.tech.muvio.dto.MovieGalleryRequestDto;
import org.cyberrealm.tech.muvio.dto.MovieVibeRequestDto;
import org.cyberrealm.tech.muvio.dto.PosterDto;
import org.cyberrealm.tech.muvio.dto.TitleDto;
import org.cyberrealm.tech.muvio.model.Movie;
import org.cyberrealm.tech.muvio.service.MovieService;
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
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;

    @GetMapping("/{id}")
    public MovieDto getMovieById(@PathVariable String id) {
        return movieService.getMovieById(id);
    }

    @PostMapping
    public Movie saveMovie(@RequestBody Movie movie) {
        return movieService.saveMovie(movie);
    }

    @DeleteMapping("/{id}")
    public void deleteMovieById(@PathVariable String id) {
        movieService.deleteMovieById(id);
    }

    @PutMapping("/{id}")
    public Movie updateMovie(@PathVariable String id, @RequestBody Movie movie) {
        return movieService.updateMovie(id, movie);
    }

    @GetMapping("/vibe")
    public Slice<MovieBaseDtoWithPoints> getAllMoviesByVibe(
            @RequestBody @Valid MovieVibeRequestDto requestDto, Pageable pageable) {
        return movieService.getAllMoviesByVibe(requestDto, pageable);
    }

    @GetMapping("/gallery")
    public Slice<MovieBaseDto> getAllForGallery(@RequestBody MovieGalleryRequestDto requestDto,
                                                Pageable pageable) {
        return movieService.getAllForGallery(requestDto, pageable);
    }

    @GetMapping("/luck/{size}")
    public Set<MovieBaseDto> getAllLuck(@PathVariable int size) {
        return movieService.getAllLuck(size);
    }

    @GetMapping("/recommendations")
    public Slice<MovieBaseDto> getRecommendations(Pageable pageable) {
        return movieService.getRecommendations(pageable);
    }

    @GetMapping("/top-list/{topList}")
    public Slice<MovieDtoWithCast> getMoviesByTopList(@PathVariable String topList,
                                                      Pageable pageable) {
        return movieService.findMoviesByTopLists(topList, pageable);
    }

    @GetMapping("/posters")
    public Slice<PosterDto> findAllPosters(Pageable pageable) {
        return movieService.findAllPosters(pageable);
    }

    @GetMapping("/titles")
    public Slice<TitleDto> findAllTitle(Pageable pageable) {
        return movieService.findAllTitles(pageable);
    }

    @GetMapping("/titles/{title}")
    public MovieDto findByTitle(@PathVariable String title) {
        return movieService.findByTitle(title);
    }
}
