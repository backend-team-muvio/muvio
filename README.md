# Backend-moodie

🎬 **Introduction**  
Backend-moodie is a Java-based application that helps users find movies or TV shows to watch based on their preferences, mood, year of release, type (movie or series), and other filters. Media content is loaded from the TMDB API and stored in a MongoDB database.

👉 💻 Live Demo: [Frontend for Backend-moodie](https://furart.github.io/Team_project_FE/#/)

---

🧰 **Technologies and Tools Used**  
- Java 21  
- Maven  
- Spring Boot  
- Mockito  
- MapStruct  
- Lombok  
- Docker  
- Swagger  
- TMDB API  
- MongoDB
---

🚀 **How to Run the Application**  

### 📦 Initial Setup (for both Docker & local run)  
1. Clone the repository:

```bash
  git clone https://github.com/backend-team-muvio/muvio.git
  cd backend-moodie
```

2. Create `.env` file:  
Rename `.env.sample` to `.env` or create a new `.env` file based on the sample provided.

3. Fill in the required environment variables:

```env
PORT=8080
SPRING_LOCAL_PORT=8081
SPRING_DOCKER_PORT=8080
DEBUG_PORT=5005
MONGO_PORT=27017
TMDB_API_TOKEN=your_tmdb_token_here
```

4. How to get TMDB API Token:
- Visit [TMDB API Settings](https://www.themoviedb.org/settings/api)  
- Log in or sign up  
- Create an API key (type: Developer or Hobby)  
- Copy the API Read Access Token (v4 auth) and paste it in `.env` under `TMDB_API_TOKEN`  

---

🐳 **Option 1: Run with Docker (recommended)**  

**Requirements:**  
- [Docker](https://www.docker.com/products/docker-desktop) installed
- [Maven 3.9+](https://maven.apache.org/install.html) – used to build the .jar

**Run the app:**  
```bash
  mvn clean package
  docker-compose up
```

📚 **Swagger UI:** [`localhost:8081/api/swagger-ui`](http://localhost:8081/api/swagger-ui/index.html#/)

---

🧪 **Option 2: Run Locally (without Docker)**  

**Requirements:**  
- [Java 21](https://jdk.java.net/21/)
- [Maven 3.9+](https://maven.apache.org/install.html)
- [MongoDB](https://www.mongodb.com/try/download/community) running locally
- Set the MONGO_URL in your .env file to:  
mongodb://localhost:27017/muvio?retryWrites=true&w=majority&connectTimeoutMS=10000&socketTimeoutMS=20000 

**Run the app:**  
```bash  
  mvn clean spring-boot:run
```

📚 Swagger UI: [`localhost:8080/api/swagger-ui`](http://localhost:8080/api/swagger-ui/index.html#/)

📌 **Port Configuration**

By default, the application runs on:
- `8081` when using **Docker**
- `8080` when running **locally**

If one of these ports is already in use (e.g., by another service), you can change it by updating the `.env` file:
```env
PORT=8082
SPRING_LOCAL_PORT=8082
```

### 📡 MediaController Endpoints

- `GET /api/media/{mediaId}` - Returns full information about a media item by its ID.  
__Example:__  
Get details of the movie with ID `12345`:
  ```bash
  curl -X GET "http://localhost:8080/api/media/12345"
  ```
- `GET /api/media/all?...` – Returns all media items. With pagination support (page, size, sort).  
__Example:__  
Get the first 10 media items, sorted by title:
   ```bash
   curl -X GET "http://localhost:8080/api/media/all?page=0&size=10&sort=title,asc"
   ```  
- `GET /api/media/count` – Returns the total number of media items stored in the database.  
__Example:__  
Get total count of media:  
  ```bash
   curl -X GET "http://localhost:8080/api/media/count"
  ```
- `GET /api/media/vibe?...` – Filter by vibe, categories, years and type. With pagination support (page, size, sort).  
__Example:__  
Get a slice of media filtered by vibe, categories, and years:
  ```bash
   curl -X GET "http://localhost:8080/api/media/vibe?page=0&size=10&vibe=MAKE_ME_FEEL_GOOD&categories=BASED_ON_A_TRUE_STORY,BASED_ON_A_BOOK,MUST_WATCH_LIST,GIRL_POWER,LIFE_CHANGING_MOVIES,IMD_TOP_250&years=2020-2025&type=MOVIE"
  ```  
- `GET /api/media/gallery?...` – Filter by partial title, years, and type. With pagination support (page, size, sort).  
__Example:__  
Get media with "game" in the title, from 2010 to 2015, of type TV_SHOW sorted by rating:
  ```bash
   curl -X GET "http://localhost:8080/api/media/gallery?page=1&size=10&sort=rating,desc&title=game&years=2010-2015&type=TV_SHOW"
  ```  
- `GET /api/media/luck/{number}` – Get random media selection.  
__Example:__  
Get 5 random media items:
  ```bash
   curl -X GET "http://localhost:8080/api/media/luck/5"
  ```    
- `GET /api/media/recommendations?...` – Get 6 recommended media items.  
__Example:__  
Get recommended media on page 0:
  ```bash
   curl -X GET "http://localhost:8080/api/media/recommendations?page=0"
  ```    
- `GET /api/media/top-list/{topList}` – Media from predefined top list. With pagination support (page, size, sort).   
__Example:__  
Get media from the "TOP_OSCAR_WINNING_MASTERPIECES" list sorted by rating:
  ```bash
   curl -X GET "http://localhost:8080/api/media/top-list/TOP_OSCAR_WINNING_MASTERPIECES?page=1&size=10"
  ```      
- `GET /api/media/posters?...` – Get list of media posters. With pagination support (page, size, sort).   
__Example:__  
Get a list of the top 100 posters sorted by rating:
  ```bash
   curl -X GET "http://localhost:8080/api/media/posters?page=0&size=100&sort=rating,desc"
  ```     
- `GET /api/media/titles?...` – Get all media titles. With pagination support (page, size, sort).  
__Example:__  
Get the first 100 titles sorted by rating:
  ```bash
   curl -X GET "http://localhost:8080/api/media/titles?page=0&size=100&sort=rating,desc"
  ```    
- `GET /api/media/titles/{title}` – Find media by exact title.  
__Example:__  
Find the movie "Inception":
  ```bash
   curl -X GET "http://localhost:8080/api/media/titles/Inception"
  ```    
- `GET /api/media/statistics` – Provide statistical info for the main page.  
__Example:__  
  ```bash
   curl -X GET "http://localhost:8080/api/media/statistics"
  ```    

**🎭Available Vibes:**  
- MAKE_ME_CHILL  
- SCARY_ME_SILLY  
- MAKE_ME_FEEL_GOOD  
- MAKE_ME_DREAM  
- MAKE_ME_CURIOUS  
- TAKE_ME_TO_ANOTHER_WORLD  
- BLOW_MY_MIND  
- KEEP_ME_ON_EDGE  

**🗂Available Categories:**  
- BASED_ON_A_TRUE_STORY  
- SPY_AND_COP_PLOTS  
- BASED_ON_A_BOOK  
- MUST_WATCH_LIST  
- GIRL_POWER  
- LIFE_CHANGING_MOVIES  
- SPORT_LIFE_PLOTS  
- IMD_TOP_250  

**🎬Available Types:**  
- MOVIE  
- TV_SHOW  
- SHORTS

**🏆 Available Top Lists:**  
- ICONIC_MOVIES_OF_THE_21ST_CENTURY  
- TOP_OSCAR_WINNING_MASTERPIECES  
- TOP_MOST_WATCHED_BLOCKBUSTERS_OF_THE_DECADE  
- TOP_100_SUPERHERO_MOVIES  
- TOP_RATED_IMDB_MOVIES_OF_ALL_TIME  
- TOP_EMMY_WINNING_MASTERPIECES

---

📄 **License**  
This project is licensed under the MIT License – see the [LICENSE](https://github.com/OleksiiKolinko/backend-moodie?tab=License-1-ov-file) file for details.