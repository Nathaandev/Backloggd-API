# 🎮 Backloggd API

A RESTful API for tracking video games, writing reviews, and managing a wishlist — inspired by [Backloggd](https://backloggd.com/). Game data is fetched in real-time from the [RAWG Video Games Database API](https://rawg.io/apidocs).

---

## 🛠️ Tech Stack

| Technology | Version |
|---|---|
| Java | 24 |
| Spring Boot | 3.4.5 |
| Spring Security | Basic Auth + BCrypt |
| Spring Data JPA | — |
| Spring WebFlux (WebClient) | — |
| MySQL | 8.0 |
| Docker | — |
| RAWG API | External |

---

## 📦 Features

- 🔍 **Game Search** — search by name, genre, developer, publisher, tags, or Metacritic score
- ✍️ **Reviews** — authenticated users can publish reviews with rating and playtime
- 💾 **Wishlist** — add, list, and remove games from a personal wishlist
- 🔐 **Authentication** — HTTP Basic Auth with BCrypt password hashing
- 🗄️ **Persistence** — game and review data cached in MySQL

---

## 🚀 Getting Started

### Prerequisites

- Java 24+
- Docker and Docker Compose
- A [RAWG API key](https://rawg.io/login?forward=developer) (free)

### 1. Clone the repository

```bash
git clone https://github.com/Nathaandev/Backloggd-API.git
cd Backloggd-API
```

### 2. Start the database

```bash
docker compose up -d
```

This starts a MySQL 8.0 container on port `3306` with a persistent volume.

### 3. Configure the application

Create an `application.properties` (or `application.yml`) under `src/main/resources/` with the following:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/<your_database>
spring.datasource.username=root
spring.datasource.password=rootd
spring.jpa.hibernate.ddl-auto=update

rawg.api.base-url=https://api.rawg.io/api
rawg.api.key=YOUR_RAWG_API_KEY
```

### 4. Run the application

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## 📡 API Endpoints

### 🔓 Public

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/signup` | Register a new user |

### 🔐 Authenticated (Basic Auth)

#### Games

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/games/search/{gameName}` | Search a game by name |
| `GET` | `/games/search/genre?genres=` | Filter games by genre |
| `GET` | `/games/search/dev?developer=` | Filter games by developer |
| `GET` | `/games/search/pub?publisher=` | Filter games by publisher |
| `GET` | `/games/search/metacritic?ordering=` | Sort games by Metacritic score |
| `GET` | `/games/search/tags?tags=` | Filter games by tags |

Paginated endpoints accept `?page=0&size=20` query parameters.

#### Reviews

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/reviews/{gameName}` | Publish a review for a game |

**Request body:**
```json
{
  "rating": 8.5,
  "review": "Great game!",
  "gameTime": 42
}
```

#### Wishlist

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/wishlist/{gameName}` | Add a game to your wishlist |
| `GET` | `/userwishlist` | Get your wishlist |
| `DELETE` | `/removefromwishlist/{gameName}` | Remove a game from your wishlist |

---

## 🗂️ Project Structure

```
src/main/java/com/example/backloggd/
├── Controller/       # REST controllers
├── Services/         # Business logic
├── Models/           # JPA entities (User, Game, Review)
├── Repository/       # Spring Data JPA interfaces
├── DTO/              # Request/Response objects
├── Util/             # Mappers and helpers
└── security/         # Spring Security configuration
```

---

## 🐳 Docker

The `docker-compose.yml` sets up the MySQL database:

```yaml
services:
  db:
    image: mysql:8.0-oracle
    container_name: mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: rootd
    ports:
      - "3306:3306"
    volumes:
      - krlsnathaan_data:/var/lib/mysql
```

---

## 📝 License

This project is for personal/portfolio use. Feel free to explore and contribute.
