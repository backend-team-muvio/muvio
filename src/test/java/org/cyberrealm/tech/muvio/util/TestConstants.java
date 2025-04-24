package org.cyberrealm.tech.muvio.util;

import org.cyberrealm.tech.muvio.model.Actor;

public final class TestConstants {
    // Media IDs і Titles
    public static final String FIRST_MEDIA_ID = "media-1";
    public static final String SECOND_MEDIA_ID = "media-2";
    public static final String THIRD_MEDIA_ID = "media-3";
    public static final String INVALID_MEDIA_ID = "invalid-media-id";
    public static final String FIRST_MEDIA_TITLE = "Inception";
    public static final String NONEXISTENT_TITLE = "Nonexistent Title";
    // Prefixes for names.
    public static final String MOVIE_PREFIX = "Movie ";
    public static final String TV_SHOW_TITLE_PREFIX = "TV Show ";
    public static final String TITLE_BASED_MOVIE_PREFIX = "Title-Based Movie ";
    public static final String FILTERED_MOVIE_PREFIX = "Filtered Movie ";
    // Popular Movie and TV - IDs and Keys.
    public static final int POPULAR_MOVIE_ID_ONE = 1;
    public static final int POPULAR_MOVIE_ID_TWO = 2;
    public static final String MOVIE_KEY_ONE = "1";
    public static final String MOVIE_KEY_TWO = "2";

    public static final int POPULAR_TV_ID_ONE = 3;
    public static final int POPULAR_TV_ID_TWO = 4;
    public static final String TV_PREFIX = "TV";
    public static final String TV_MOVIE_KEY_ONE = TV_PREFIX + POPULAR_TV_ID_ONE;
    public static final String TV_MOVIE_KEY_TWO = TV_PREFIX + POPULAR_TV_ID_TWO;
    // Filtered Movie - IDs and Keys.
    public static final int FILTERED_MOVIE_ID_ONE = 5;
    public static final int FILTERED_MOVIE_ID_TWO = 6;
    public static final String FILTERED_MOVIE_KEY_ONE = "5";
    public static final String FILTERED_MOVIE_KEY_TWO = "6";
    // Search Movie - IDs and Keys.
    public static final int SEARCH_MOVIE_ID = 7;
    public static final String SEARCH_MOVIE_KEY = "7";
    // Global settings
    public static final String EN_LANGUAGE = "en";
    public static final String US_REGION = "US";
    public static final int CURRENT_YEAR = 2025;
    // Error and status messages.
    public static final String SERVICE_UNAVAILABLE_MESSAGE = "Service unavailable";
    // Release years and durations.
    public static final int RELEASE_YEAR_2022 = 2022;
    public static final int RELEASE_YEAR_2010 = 2010;
    public static final int RELEASE_YEAR_1999 = 1999;
    public static final int FIRST_POPULAR_MEDIA_DURATION = 100;
    public static final int INTERCEPTION_DURATION = 148;
    // Other
    public static final String EMPTY = "";
    public static final boolean IS_MOVIES = true;
    public static final boolean IS_TV_SHOW = false;
    // Details of the first movie (Inception).
    public static final String FIRST_MOVIE_TITLE = "Inception";
    public static final double FIRST_MOVIE_RATING = 8.8;
    public static final String FIRST_MOVIE_TRAILER = "https://youtube.com/trailer";
    public static final String POSTER_INCEPTION_URL = "/poster/inception.jpg";
    public static final String PHOTOS_INCEPTION_1_URL = "/photos/inception1.jpg";
    public static final String PHOTOS_INCEPTION_2_URL = "/photos/inception2.jpg";
    public static final String FIRST_MOVIE_OVERVIEW = "A thief who steals corporate secrets ...";
    public static final String CHRISTOPHER_NOLAN = "Christopher Nolan";
    // Actor details.
    public static final String FIRST_ACTOR_ID = "12s";
    public static final String FIRST_ACTOR_ROLE = "Max";
    public static final Actor DEFAULT_ACTOR = new Actor();
    // Details of the second movie (The Matrix)
    public static final String SECOND_MOVIE_TITLE = "The Matrix";
    public static final double SECOND_MOVIE_RATING = 8.7;
    public static final String SECOND_MOVIE_TRAILER = "https://youtube.com/trailer2";
    public static final String SECOND_MOVIE_POSTER = "/poster/matrix.jpg";
    public static final int SECOND_MOVIE_DURATION = 136;
    public static final String SECOND_MOVIE_DIRECTOR = "Lana Wachowski";
    public static final String PHOTOS_MATRIX_1_URL = "/photos/matrix1.jpg";
    public static final String PHOTOS_MATRIX_2_URL = "/photos/matrix2.jpg";
    public static final String SECOND_MOVIE_OVERVIEW = "A computer hacker learns from mysterious "
            + "rebels ...";
    // Expected values and counters.
    public static final int EXPECTED_COUNT = 2;
    public static final int EMPTY_COUNT = 0;
    public static final int FIRST_RECORD = 0;
    public static final int ZERO_OF_RECORDS = FIRST_RECORD;
    public static final int EXPECTED_SIZE_ONE = 1;
    public static final int EXPECTED_SIZE_TWO = 2;
    public static final int EXPECTED_SIZE_THREE = 3;
    // Parameter names and related constants.
    public static final String PARAM_NAME_VIBE = "vibe";
    public static final String PARAM_NAME_YEARS = "years";
    public static final String PARAM_NAME_TYPE = "type";
    public static final String PARAM_NAME_CATEGORIES = "categories";
    public static final String PARAM_NAME_PAGE = "page";
    public static final String PARAM_NAME_SIZE = "size";
    public static final String PAGE_SIZE_FIVE = "5";
    public static final String PAGE_NUMBER_ZERO = "0";
    public static final String NON_EXISTENT_VIBE = "nonexistent";
    public static final String INVALID_YEARS_PERIOD = "1900";
    public static final String INVALID_CATEGORIES = "Unknown";
    public static final String PARAM_NAME_TITLE = "title";
    public static final String CRITERIA_YEARS_PERIOD = "2010-2015";
    public static final String CONTENT_STRING = "content";

    private TestConstants() {
    }
}
