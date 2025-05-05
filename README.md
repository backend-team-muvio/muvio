# Backend-moodie
## Introduction
Backend-moodie is a Java-based application that helps users find movies or TV shows to watch based on their preferences, mood, year of release, type (movie or series), and other filters. Media content is loaded from the TMDB API and stored in the database.

Explore the deployed frontend: https://furart.github.io/Team_project_FE/#/
## The technologies and tools used
* Java 21
* Maven
* Mockito
* Mapstruct
* Spring Boot
* Lombok
* Docker
* Swagger
* TMDB API
## Endpoints
### Media Controller
* GET: /api/media/{mediaId} - Get full information about a media item by its ID.
* GET: /api/media/all - Returns all media items in a simplified format (MediaBaseDto) with pagination support (page, size, sort).
* GET: /api/media/count - Returns the total number of media items stored in the database.
* GET: /api/media/api/media/vibe?page={0}&size={10}&sort=points,desc&sort=rating,desc&vibe={MAKE_ME_FEEL_GOOD}&categories={BASED_ON_A_TRUE_STORY,BASED_ON_A_BOOK, MUST_WATCH_LIST,GIRL_POWER,LIFE_CHANGING_MOVIES, IMD_TOP_250}&years={2020-2025}&type={MOVIE} - Returns a paginated slice of media items filtered by mood-based ("vibe") criteria. Available vibes: MAKE_ME_CHILL, SCARY_ME_SILLY, MAKE_ME_FEEL_GOOD, MAKE_ME_DREAM,MAKE_ME_CURIOUS, TAKE_ME_TO_ANOTHER_WORLD, BLOW_MY_MIND, KEEP_ME_ON_EDGE. Available categories: BASED_ON_A_TRUE_STORY, SPY_AND_COP_PLOTS, BASED_ON_A_BOOK, MUST_WATCH_LIST, GIRL_POWER, LIFE_CHANGING_MOVIES,SPORT_LIFE_PLOTS, IMD_TOP_250. Available types: MOVIE, TV_SHOW, SHORTS.
* GET: /api/media/gallery?page={1}&size={10}&sort={rating,desc}&title={part of title}&years={2000-2025}&type={TV_SHOW} - Returns a paginated slice of media items filtered by part of title, years and type. Available types: MOVIE, TV_SHOW, SHORTS.
* GET: /api/media/luck/{number of items} - Returns a random selection of media items.
* GET: /api/media/recommendations?page={0} - Returns a paginated list of recommended media items with size six.
* GET: /api/media/top-list/{topList} - Returns media items that belong to a specific top list. Available top list: ICONIC_MOVIES_OF_THE_21ST_CENTURY,TOP_OSCAR_WINNING_MASTERPIECES, TOP_MOST_WATCHED_BLOCKBUSTERS_OF_THE_DECADE, TOP_100_SUPERHERO_MOVIES, TOP_RATED_IMDB_MOVIES_OF_All_TIME TOP_EMMY_WINNING_MASTERPIECES.
* GET: /api/media/posters?page={0}&size={100}&sort={rating,desc} - Returns a paginated list of media posters.
* GET: /api/media/titles?page={0}&size={100}&sort={rating,desc} - Returns a paginated list of all media titles.
* GET: /api/media/titles/{title} - Finds a media item by its exact title.
## How to use the application
1. Make sure you have installed next tools:
* Docker
2. Clone the repository from GitHub
3. Rename .env.sample to .env or create new file .env and copy fields from .env.sample to new file.
4. Then need to fill in these fields.
* In most situations ports put: PORT=8080, SPRING_LOCAL_PORT=8081, SPRING_DOCKER_PORT=8080, DEBUG_PORT=5005, MONGO_PORT=27017.
* TMDB_API_TOKEN= - You must registered on TMDB and generated API Read Access Token and put in this field. Link: https://www.themoviedb.org/settings/api
5. In docker-compose.yml you must uncommitted mongodb.
6. Run the following commands in command line:
```json
cd "path of backend-moodie on the pc"
mvn clean package
docker-compose up
```
Swagger is available for testing at http://localhost:{SPRING_LOCAL_PORT}/api/swagger-ui/index.html#/.
