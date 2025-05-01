package org.cyberrealm.tech.muvio.util;

import static org.cyberrealm.tech.muvio.util.TestConstants.CHRISTOPHER_NOLAN;
import static org.cyberrealm.tech.muvio.util.TestConstants.DEFAULT_ACTOR;
import static org.cyberrealm.tech.muvio.util.TestConstants.FIRST_ACTOR_ID;
import static org.cyberrealm.tech.muvio.util.TestConstants.FIRST_ACTOR_ROLE;
import static org.cyberrealm.tech.muvio.util.TestConstants.FIRST_MEDIA_ID;
import static org.cyberrealm.tech.muvio.util.TestConstants.FIRST_MOVIE_OVERVIEW;
import static org.cyberrealm.tech.muvio.util.TestConstants.FIRST_MOVIE_RATING;
import static org.cyberrealm.tech.muvio.util.TestConstants.FIRST_MOVIE_TITLE;
import static org.cyberrealm.tech.muvio.util.TestConstants.FIRST_MOVIE_TRAILER;
import static org.cyberrealm.tech.muvio.util.TestConstants.INTERCEPTION_DURATION;
import static org.cyberrealm.tech.muvio.util.TestConstants.PHOTOS_INCEPTION_1_URL;
import static org.cyberrealm.tech.muvio.util.TestConstants.PHOTOS_INCEPTION_2_URL;
import static org.cyberrealm.tech.muvio.util.TestConstants.PHOTOS_MATRIX_1_URL;
import static org.cyberrealm.tech.muvio.util.TestConstants.PHOTOS_MATRIX_2_URL;
import static org.cyberrealm.tech.muvio.util.TestConstants.POSTER_INCEPTION_URL;
import static org.cyberrealm.tech.muvio.util.TestConstants.RELEASE_YEAR_1999;
import static org.cyberrealm.tech.muvio.util.TestConstants.RELEASE_YEAR_2010;
import static org.cyberrealm.tech.muvio.util.TestConstants.SECOND_MEDIA_ID;
import static org.cyberrealm.tech.muvio.util.TestConstants.SECOND_MOVIE_DIRECTOR;
import static org.cyberrealm.tech.muvio.util.TestConstants.SECOND_MOVIE_DURATION;
import static org.cyberrealm.tech.muvio.util.TestConstants.SECOND_MOVIE_OVERVIEW;
import static org.cyberrealm.tech.muvio.util.TestConstants.SECOND_MOVIE_POSTER;
import static org.cyberrealm.tech.muvio.util.TestConstants.SECOND_MOVIE_RATING;
import static org.cyberrealm.tech.muvio.util.TestConstants.SECOND_MOVIE_TITLE;
import static org.cyberrealm.tech.muvio.util.TestConstants.SECOND_MOVIE_TRAILER;

import java.util.List;
import java.util.Set;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.cyberrealm.tech.muvio.model.Media;
import org.cyberrealm.tech.muvio.model.RoleActor;
import org.cyberrealm.tech.muvio.model.TopLists;
import org.cyberrealm.tech.muvio.model.Type;
import org.cyberrealm.tech.muvio.model.Vibe;
import org.springframework.data.domain.Pageable;

public final class TestUtil {
    public static final RoleActor firstActor = new RoleActor();

    static {
        firstActor.setId(FIRST_ACTOR_ID);
        firstActor.setRole(FIRST_ACTOR_ROLE);
        firstActor.setActor(DEFAULT_ACTOR);
    }

    public static final Media firstMedia = Media.builder()
            .id(FIRST_MEDIA_ID)
            .title(FIRST_MOVIE_TITLE)
            .genres(Set.of(GenreEntity.ACTION, GenreEntity.SCIENCE_FICTION))
            .rating(FIRST_MOVIE_RATING)
            .trailer(FIRST_MOVIE_TRAILER)
            .posterPath(POSTER_INCEPTION_URL)
            .duration(INTERCEPTION_DURATION)
            .director(CHRISTOPHER_NOLAN)
            .photos(Set.of(PHOTOS_INCEPTION_1_URL, PHOTOS_INCEPTION_2_URL))
            .actors(List.of(firstActor))
            .reviews(List.of())
            .releaseYear(RELEASE_YEAR_2010)
            .overview(FIRST_MOVIE_OVERVIEW)
            .type(Type.MOVIE)
            .vibes(Set.of(Vibe.MAKE_ME_FEEL_GOOD))
            .topLists(Set.of(TopLists.TOP_RATED_IMDB_MOVIES_OF_All_TIME))
            .build();

    public static final Media secondMedia = Media.builder()
            .id(SECOND_MEDIA_ID)
            .title(SECOND_MOVIE_TITLE)
            .genres(Set.of(GenreEntity.ACTION, GenreEntity.SCIENCE_FICTION))
            .rating(SECOND_MOVIE_RATING)
            .trailer(SECOND_MOVIE_TRAILER)
            .posterPath(SECOND_MOVIE_POSTER)
            .duration(SECOND_MOVIE_DURATION)
            .director(SECOND_MOVIE_DIRECTOR)
            .photos(Set.of(PHOTOS_MATRIX_1_URL, PHOTOS_MATRIX_2_URL))
            .actors(List.of(firstActor))
            .reviews(List.of())
            .releaseYear(RELEASE_YEAR_1999)
            .overview(SECOND_MOVIE_OVERVIEW)
            .type(Type.MOVIE)
            .build();

    public static final Pageable PAGEABLE = Pageable.unpaged();

    private TestUtil() {
    }
}
