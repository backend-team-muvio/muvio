package org.cyberrealm.tech.muvio.service.impl;

import com.uwetrottmann.tmdb2.Tmdb;
import com.uwetrottmann.tmdb2.entities.BaseMovie;
import com.uwetrottmann.tmdb2.entities.CastMember;
import com.uwetrottmann.tmdb2.entities.Credits;
import com.uwetrottmann.tmdb2.entities.CrewMember;
import com.uwetrottmann.tmdb2.entities.Genre;
import com.uwetrottmann.tmdb2.entities.GenreResults;
import com.uwetrottmann.tmdb2.entities.Images;
import com.uwetrottmann.tmdb2.entities.MovieResultsPage;
import com.uwetrottmann.tmdb2.entities.Videos;
import com.uwetrottmann.tmdb2.services.MoviesService;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cyberrealm.tech.muvio.dto.MovieDto;
import org.cyberrealm.tech.muvio.mapper.MovieMapper;
import org.cyberrealm.tech.muvio.model.Actor;
import org.cyberrealm.tech.muvio.model.Duration;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.cyberrealm.tech.muvio.model.Movie;
import org.cyberrealm.tech.muvio.model.Photo;
import org.cyberrealm.tech.muvio.model.Producer;
import org.cyberrealm.tech.muvio.model.Rating;
import org.cyberrealm.tech.muvio.model.Year;
import org.cyberrealm.tech.muvio.repository.actors.ActorRepository;
import org.cyberrealm.tech.muvio.repository.durations.DurationRepository;
import org.cyberrealm.tech.muvio.repository.genres.GenreRepository;
import org.cyberrealm.tech.muvio.repository.movies.MovieRepository;
import org.cyberrealm.tech.muvio.repository.photos.PhotoRepository;
import org.cyberrealm.tech.muvio.repository.producer.ProducerRepository;
import org.cyberrealm.tech.muvio.repository.ratings.RatingRepository;
import org.cyberrealm.tech.muvio.repository.years.YearRepository;
import org.cyberrealm.tech.muvio.service.MovieService;
import retrofit2.Response;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieServiceImpl implements MovieService {
    private static final String TRAILER = "Trailer";
    private static final String PRODUCER = "Producer";
    private static final String EN = "en";
    private static final String US = "US";
    private static final String IMAGE_PATH = "https://image.tmdb.org/t/p/w500";
    private static final String YOUTUBE_PATH = "https://www.youtube.com/watch?v=";
    private static final int PAGES_TO_LOAD = 5;

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ProducerRepository producerRepository;
    private final ActorRepository actorRepository;
    private final RatingRepository ratingRepository;
    private final YearRepository yearRepository;
    private final DurationRepository durationRepository;
    private final PhotoRepository photoRepository;
    private final MovieMapper movieMapper;
    private final Tmdb tmdb;

    @PostConstruct
    @Override
    public void importMovies() {
        movieRepository.deleteAll();
        loadGenres();
        fetchAndSaveMovies();
    }

    private Photo getPosterPath(String path) {
        if (photoRepository.findById(path).isPresent()) {
            return photoRepository.findById(path).get();
        }
        final Photo photo = new Photo();
        photo.setPath(path);
        return photoRepository.save(photo);
    }

    private Duration getDuration(Integer runtime) {
        final String id = String.valueOf(runtime);
        if (durationRepository.findById(id).isPresent()) {
            return durationRepository.findById(id).get();
        }
        final Duration duration = new Duration();
        duration.setDuration(id);
        return durationRepository.save(duration);
    }

    private Rating getRating(Double rating) {
        final String id = String.valueOf(rating);
        if (ratingRepository.findById(id).isPresent()) {
            return ratingRepository.findById(id).get();
        } else {
            final Rating ratingDb = new Rating();
            ratingDb.setRating(id);
            return ratingRepository.save(ratingDb);
        }
    }

    private Set<Photo> getPhotos(Images images) {
        if (images != null && images.backdrops != null) {
            return images.backdrops.stream().map(i -> {
                final String id = IMAGE_PATH + i.file_path;
                if (photoRepository.findById(id).isPresent()) {
                    return photoRepository.findById(id).get();
                }
                final Photo photo = new Photo();
                photo.setPath(id);
                return photoRepository.save(photo);
            }).collect(Collectors.toSet());
        }
        return null;
    }

    private Set<Actor> getActors(List<CastMember> cast) {
        return cast.stream().map(a -> {
            final String actorsId = String.valueOf(a.id);
            if (actorRepository.findById(actorsId).isPresent()) {
                return actorRepository.findById(actorsId).get();
            } else {
                final Actor actor = new Actor();
                actor.setName(a.name);
                if (a.profile_path != null) {
                    actor.setPhoto(IMAGE_PATH + a.profile_path);
                }
                return actorRepository.save(actor);
            }
        }).collect(Collectors.toSet());
    }

    private Producer getProducer(List<CrewMember> crew) {
        final Optional<CrewMember> producer = crew.stream()
                .filter(member -> PRODUCER.equals(member.job))
                .findFirst();
        if (producer.isPresent()) {
            final CrewMember producerMember = producer.get();
            final String producerName = producerMember.name;
            if (producerName != null && producerRepository.findById(producerName).isPresent()) {
                return producerRepository.findById(producerName).get();
            } else {
                final Producer producerDb = new Producer();
                producerDb.setName(producerName);
                return producerRepository.save(producerDb);
            }
        }
        return null;
    }

    private Year getReleaseYear(Date releaseDate) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(releaseDate);
        final String yearId = String.valueOf(calendar.get(Calendar.YEAR));
        if (yearRepository.findById(yearId).isPresent()) {
            return yearRepository.findById(yearId).get();
        }
        final Year yearDb = new Year();
        yearDb.setYear(yearId);
        return yearRepository.save(yearDb);
    }

    @Override
    public List<MovieDto> getAllMovies() {
        return movieRepository.findAll().stream().map(movieMapper::toMovieDto).toList();
    }

    @Override
    public Movie getMovieById(String id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("There is no movie with this id: " + id));
    }

    @Override
    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    @Override
    public void deleteMovieById(String id) {
        movieRepository.deleteById(id);
    }

    @Override
    public Movie updateMovie(String id, Movie updatedMovie) {
        Movie movie = getMovieById(id);
        movie.setName(updatedMovie.getName());
        movie.setGenres(updatedMovie.getGenres());
        movie.setRating(updatedMovie.getRating());
        movie.setTrailer(updatedMovie.getTrailer());
        movie.setPosterPath(getPosterPath(IMAGE_PATH + updatedMovie.getPosterPath()));
        return movieRepository.save(movie);
    }

    private void loadGenres() {
        genreRepository.deleteAll();
        try {
            Response<GenreResults> response = tmdb.genreService().movie(EN).execute();
            if (response.isSuccessful() && response.body() != null) {
                List<GenreEntity> genres = response.body().genres.stream()
                        .map(genre -> {
                            GenreEntity genreEntity = new GenreEntity();
                            genreEntity.setId(String.valueOf(genre.id));
                            genreEntity.setName(genre.name);
                            return genreEntity;
                        })
                        .toList();
                genreRepository.saveAll(genres);
            }
        } catch (IOException e) {
            log.error("Failed to fetch genres", e);
            throw new RuntimeException("Failed to fetch genres", e);
        }
    }

    private void fetchAndSaveMovies() {
        MoviesService moviesService = tmdb.moviesService();
        for (int page = 1; page <= PAGES_TO_LOAD; page++) {
            try {
                Response<MovieResultsPage> response = moviesService.popular(page, EN, US).execute();
                if (response.isSuccessful() && response.body() != null) {
                    saveMovies(response.body().results);
                }
            } catch (IOException e) {
                log.error("Failed to fetch movies for page {}", page, e);
            }
        }
    }

    private void saveMovies(List<BaseMovie> movies) {
        for (BaseMovie baseMovie : movies) {
            Movie movie = new Movie();
            movie.setName(baseMovie.title);
            movie.setPosterPath(IMAGE_PATH + baseMovie.poster_path);
            movie.setGenres(mapGenres(baseMovie.genre_ids));
            movie.setTrailer(fetchTrailer(baseMovie.id));
            movie.setRating(baseMovie.vote_average);
            movieRepository.save(movie);
        }
    }

    private Set<GenreEntity> mapGenres(List<Integer> genreIds) {
        return genreIds.stream()
                .map(id -> genreRepository.findById(String.valueOf(id))
                        .orElseThrow(() -> new RuntimeException("Can't find genre by id " + id)))
                .collect(Collectors.toSet());
    }

    private String fetchTrailer(Integer movieId) {
        if (movieId == null) {
            return null;
        }
        try {
            Response<Videos> response = tmdb.moviesService().videos(movieId).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body().results.stream()
                        .filter(video -> TRAILER.equals(video.type))
                        .findFirst()
                        .map(video -> YOUTUBE_PATH + video.key)
                        .orElse(null);
            }
        } catch (IOException e) {
            log.error("Failed to fetch trailer for movie {}", movieId, e);
        }
        return null;
    }
}
