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
import com.uwetrottmann.tmdb2.services.GenresService;
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
import org.springframework.stereotype.Service;
import retrofit2.Response;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {
    private static final String TRAILER = "Trailer";
    private static final String PRODUCER = "Producer";
    private static final String EN = "en";
    private static final String US = "US";
    private static final String IMAGE_PATH = "https://image.tmdb.org/t/p/w500";
    private static final String YOUTUBE_PATH = "https://www.youtube.com/watch?v=";
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int FIVE = 5;
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
        if (movieRepository != null) {
            movieRepository.deleteAll();
        }
        if (producerRepository != null) {
            producerRepository.deleteAll();
        }
        if (actorRepository != null) {
            actorRepository.deleteAll();
        }
        if (ratingRepository != null) {
            ratingRepository.deleteAll();
        }
        if (yearRepository != null) {
            yearRepository.deleteAll();
        }
        if (durationRepository != null) {
            durationRepository.deleteAll();
        }
        if (photoRepository != null) {
            photoRepository.deleteAll();
        }
        loadGenres();
        final MoviesService moviesService = tmdb.moviesService();
        for (int page = ONE; page <= FIVE; page++) {
            final Response<MovieResultsPage> response;
            try {
                response = moviesService.popular(page, EN,US).execute();
            } catch (IOException e) {
                throw new RuntimeException("Failed to fetch popular movies");
            }
            final MovieResultsPage movieResultsPage = response.body();
            if (movieResultsPage != null) {
                final List<BaseMovie> movies = movieResultsPage.results;
                if (movies != null) {
                    for (BaseMovie movie : movies) {
                        final Movie movieDb = new Movie();
                        movieDb.setName(movie.title);
                        movieDb.setPosterPath(getPosterPath(IMAGE_PATH + movie.poster_path));
                        if (movie.genre_ids != null && !movie.genre_ids.isEmpty()) {
                            final Set<GenreEntity> genres = movie.genre_ids.stream()
                                    .map(genreId -> genreRepository.findById(
                                            String.valueOf(genreId)).orElseThrow(
                                                    () -> new RuntimeException(
                                                    "Can't find genre by id " + genreId)))
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toSet());
                            movieDb.setGenres(genres);
                        }
                        if (movie.id != null) {
                            final Response<Videos> videosResponse;
                            try {
                                videosResponse = moviesService.videos(movie.id, EN).execute();
                            } catch (IOException e) {
                                throw new RuntimeException("Can not find videos");
                            }
                            final List<Videos.Video> trailers = Objects.requireNonNull(
                                            Objects.requireNonNull(videosResponse.body()).results)
                                    .stream().filter(video -> {
                                        if (video.type != null) {
                                            return TRAILER.equals(video.type.toString());
                                        }
                                        return false;
                                    })
                                    .toList();
                            if (!trailers.isEmpty()) {
                                movieDb.setTrailer(YOUTUBE_PATH + trailers.get(ZERO).key);
                            }
                        }
                        if (movie.vote_average != null) {
                            movieDb.setRating(getRating(movie.vote_average));
                        }
                        final Integer movieId = movie.id;
                        movieDb.setId(movieId.toString());
                        movieDb.setReleaseYear(getReleaseYear(movie.release_date));
                        movieDb.setOverview(movie.overview);
                        Response<Credits> movieDetailsResponse;
                        Response<com.uwetrottmann.tmdb2.entities.Movie> movieResponse;
                        Response<Images> imagesResponse;
                        try {
                            movieDetailsResponse = moviesService.credits(movie.id).execute();
                            movieResponse = moviesService.summary(movieId, EN).execute();
                            imagesResponse = moviesService.images(movieId, EN).execute();
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to fetch movie details", e);
                        }
                        final Credits credits = movieDetailsResponse.body();
                        final com.uwetrottmann.tmdb2.entities.Movie movieTm = movieResponse.body();
                        if (credits != null) {
                            movieDb.setProducer(getProducer(Objects.requireNonNull(credits.crew)));
                            movieDb.setActors(getActors(Objects.requireNonNull(credits.cast)));
                        }
                        if (movieTm != null && movieTm.runtime != null) {
                            movieDb.setDuration(getDuration(movieTm.runtime));
                        }
                        movieDb.setPhotos(getPhotos(imagesResponse.body()));
                        Objects.requireNonNull(movieRepository).save(movieDb);
                    }
                }
            }
        }
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
        final Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("There is no movie with this id: " + id));
        movie.setName(updatedMovie.getName());
        movie.setGenres(updatedMovie.getGenres());
        movie.setRating(updatedMovie.getRating());
        movie.setTrailer(updatedMovie.getTrailer());
        movie.setPosterPath(getPosterPath(IMAGE_PATH + updatedMovie.getPosterPath()));
        return movieRepository.save(movie);
    }

    private void loadGenres() {
        if (genreRepository != null) {
            genreRepository.deleteAll();
        }
        final GenresService genresService = tmdb.genreService();
        final Response<GenreResults> response;
        try {
            response = genresService.movie(EN).execute();
        } catch (IOException e) {
            throw new RuntimeException("Failed execute genres");
        }
        if (response.body() != null) {
            final List<Genre> genreEntities = response.body().genres;
            for (Genre genre : Objects.requireNonNull(genreEntities)) {
                final GenreEntity genreEntity = new GenreEntity();
                genreEntity.setId(String.valueOf(genre.id));
                genreEntity.setName(genre.name);
                Objects.requireNonNull(genreRepository).save(genreEntity);
            }
        }
    }
}

