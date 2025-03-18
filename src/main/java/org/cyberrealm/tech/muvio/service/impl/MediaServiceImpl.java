package org.cyberrealm.tech.muvio.service.impl;

import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.cyberrealm.tech.muvio.dto.MediaDto;
import org.cyberrealm.tech.muvio.dto.MovieBaseDto;
import org.cyberrealm.tech.muvio.dto.MovieBaseDtoWithPoints;
import org.cyberrealm.tech.muvio.dto.MovieDtoWithCast;
import org.cyberrealm.tech.muvio.dto.MovieDtoWithCastFromDb;
import org.cyberrealm.tech.muvio.dto.MovieGalleryRequestDto;
import org.cyberrealm.tech.muvio.dto.MovieVibeRequestDto;
import org.cyberrealm.tech.muvio.dto.PosterDto;
import org.cyberrealm.tech.muvio.dto.TitleDto;
import org.cyberrealm.tech.muvio.exception.EntityNotFoundException;
import org.cyberrealm.tech.muvio.mapper.MediaMapper;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.model.Type;
import org.cyberrealm.tech.muvio.repository.media.MediaRepository;
import org.cyberrealm.tech.muvio.service.MediaService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {
    private static final int NINE = 9;
    private static final String DEFAULT_TITLE = "";
    private static final String SPLIT_PATTERN = "-";
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;
    private static final int DEFAULT_YEAR = 1900;
    private static final String RATING = "rating";
    private static final List<String> TOP_GENRES = List.of("ACTION", "DRAMA", "COMEDY");
    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;

    @Override
    public MediaDto getMovieById(String id) {
        return mediaMapper.toMovieDto(mediaRepository.findMovieById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no movie with this id: "
                        + id)));
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
    public Media updateMovie(String id, Media updatedMovie) {
        final Media movie = mediaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no movie with this id: "
                        + id));
        movie.setTitle(updatedMovie.getTitle());
        movie.setGenres(updatedMovie.getGenres());
        movie.setRating(updatedMovie.getRating());
        movie.setTrailer(updatedMovie.getTrailer());
        return mediaRepository.save(movie);
    }

    @Override
    public Slice<MovieBaseDtoWithPoints> getAllMoviesByVibe(MovieVibeRequestDto requestDto,
                                                            Pageable pageable) {
        final Integer[] years = getYearsIn(requestDto.years());
        final Set<String> categories;
        if (requestDto.categories() == null) {
            categories = Set.of();
        } else {
            categories = requestDto.categories();
        }
        final Slice<MovieBaseDtoWithPoints> moviesByVibes = mediaRepository.findAllByVibes(
                years[ZERO], years[ONE], getType(requestDto.type()),
                requestDto.vibe(), categories, pageable);
        updateDuration(moviesByVibes);
        return moviesByVibes;
    }

    @Override
    public Slice<MovieBaseDto> getAllForGallery(MovieGalleryRequestDto requestDto,
                                                Pageable pageable) {
        final Integer[] years = getYearsIn(requestDto.years());
        final String title;
        if (requestDto.title() != null) {
            title = requestDto.title();
        } else {
            title = DEFAULT_TITLE;
        }
        final Slice<MovieBaseDto> moviesForGallery = mediaRepository.getAllForGallery(years[ZERO],
                years[ONE], title, getType(requestDto.type()), pageable);
        updateDuration(moviesForGallery);
        return moviesForGallery;
    }

    @Override
    public Set<MovieBaseDto> getAllLuck(int size) {
        final Set<MovieBaseDto> luck = mediaRepository.getAllLuck(size);
        updateDuration(luck);
        return luck;
    }

    @Transactional
    @Override
    public Slice<MovieBaseDto> getRecommendations(Pageable pageable) {
        int minYear = Year.now().getValue() - THREE;
        final List<MovieBaseDto> recommendations = new ArrayList<>();
        TOP_GENRES.forEach(genre -> {
            addRecommendationsByTypeAndGenre(Type.MOVIE, genre, minYear, pageable,
                    recommendations);
            addRecommendationsByTypeAndGenre(Type.TV_SHOW, genre, minYear, pageable,
                    recommendations);
        });
        if (recommendations.size() != 6) {
            return null;
        }
        updateDuration(recommendations);
        return new SliceImpl<>(recommendations, pageable, !recommendations.isEmpty());
    }

    @Override
    public Slice<MovieDtoWithCast> findMoviesByTopLists(String topList, Pageable pageable) {
        final Slice<MovieDtoWithCastFromDb> movies = mediaRepository
                .findByTopListsContaining(topList, pageable);
        final List<MovieDtoWithCast> moviesList = movies.stream()
                .map(mediaMapper::toMovieDtoWithCast).toList();
        return new SliceImpl<>(moviesList, pageable, !moviesList.isEmpty());
    }

    @Override
    public Slice<PosterDto> findAllPosters(Pageable pageable) {
        return mediaRepository.findAllPosters(pageable);
    }

    @Override
    public Slice<TitleDto> findAllTitles(Pageable pageable) {
        return mediaRepository.findAllTitles(pageable);
    }

    @Override
    public MediaDto findByTitle(String title) {
        return mediaMapper.toMovieDto(mediaRepository.findByTitle(title));
    }

    private void addRecommendationsByTypeAndGenre(
            Type type, String genre, int minYear, Pageable pageable,
            List<MovieBaseDto> recommendations) {
        List<MovieBaseDto> movieList = fetchMovies(type, genre, minYear, pageable);
        if (!movieList.isEmpty() && recommendations.contains(movieList.getFirst())) {
            movieList = fetchMovies(type, genre, minYear, pageable.next());
        }
        recommendations.addAll(movieList);
    }

    private List<MovieBaseDto> fetchMovies(Type type, String genre, int minYear,
                                           Pageable pageable) {
        Pageable sortedPageRequest = PageRequest.of(pageable.getPageNumber(),
                pageable.getPageSize(), Sort.by(RATING).descending());
        return mediaRepository.findMoviesByTypeGenreAndYears(type, genre, minYear,
                sortedPageRequest).getContent();
    }

    private Set<String> getType(String type) {
        if (type != null && !type.isBlank()) {
            return Set.of(type);
        }
        return Arrays.stream(Type.values()).map(Enum::toString).collect(Collectors.toSet());
    }

    private Integer[] getYearsIn(String years) {
        if (years != null && years.length() == NINE
                && years.contains(SPLIT_PATTERN)) {
            return Arrays.stream(years.split(SPLIT_PATTERN)).map(Integer::parseInt)
                    .toArray(Integer[]::new);
        } else {
            final Integer[] yearsDefault = new Integer[TWO];
            yearsDefault[ZERO] = DEFAULT_YEAR;
            yearsDefault[ONE] = Year.now().getValue();
            return yearsDefault;
        }
    }

    private <T extends MovieBaseDto> void updateDuration(Iterable<T> movies) {
        movies.forEach(movie -> movie.setDuration(
                mediaMapper.toDuration(Integer.parseInt(movie.getDuration()))));
    }
}
