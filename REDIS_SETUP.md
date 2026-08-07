# Redis Cache Setup Guide

## Overview
This project now uses Redis for caching IGDB API responses to improve performance and reduce API calls.

## Prerequisites
- Redis server installed and running locally on port 6379
- Maven dependencies added (spring-boot-starter-cache, spring-boot-starter-data-redis)

## Installation

### Windows (using Chocolatey)
```powershell
choco install redis-64
```

### Windows (using WSL)
```bash
sudo apt-get install redis-server
sudo service redis-server start
```

### macOS
```bash
brew install redis
brew services start redis
```

### Linux
```bash
sudo apt-get install redis-server
sudo systemctl start redis
```

## Configuration

### 1. Start Redis Server
Make sure Redis is running before starting the application:
```bash
redis-server
```

### 2. Configure Application
The Redis configuration is in `application-redis.properties`:
```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.timeout=2000ms
```

To enable Redis, add this profile when running:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=redis
```

Or set it as default in your main `application.properties`:
```properties
spring.profiles.active=redis
```

## Cache Configuration

### Cache Settings (CacheConfig.java)
- **TTL**: 6 hours for all cached entries
- **Null values**: Disabled (not cached)
- **Serialization**: JSON format

### Cached Operations
- `games`: Search games by name
- `gameDetails`: Get game details by ID
- `gamesByGenre`: Search games by genre (with pagination)
- `gamesByDeveloper`: Search games by developer (with pagination)
- `gamesByPublisher`: Search games by publisher (with pagination)
- `gamesByMetacritic`: Search games by metacritic rating (with pagination)
- `gamesByTags`: Search games by tags (with pagination)

## Scheduled Cache Warmup

### Jobs
1. **Popular Games Warmup** (Daily at 3 AM)
   - Pre-loads top 100 games by metacritic rating
   - 10 pages × 10 games each

2. **Popular Genres Warmup** (Every 6 hours)
   - Pre-loads games from popular genres: Action, RPG, Adventure, Strategy, Shooter
   - 12 games per genre

### Disable Scheduling
To disable scheduled jobs, remove `@EnableScheduling` from `BackloggdApplication.java`

## Testing the Cache

### Manual Testing
1. Start Redis server
2. Start the application with Redis profile
3. Make a request to any game endpoint
4. Check Redis CLI:
```bash
redis-cli
KEYS *
```
5. Make the same request again - it should be faster (cached)

### Monitor Cache Hits
Add logging to monitor cache performance in `application.properties`:
```properties
logging.level.org.springframework.cache=DEBUG
```

## Troubleshooting

### Redis Connection Failed
- Check if Redis is running: `redis-cli ping` (should return PONG)
- Verify host and port in configuration
- Check firewall settings

### Cache Not Working
- Ensure `@EnableCaching` is present in `CacheConfig.java`
- Check that methods have `@Cacheable` annotations
- Verify Redis is accessible from the application

### Performance Issues
- Adjust TTL in `CacheConfig.java` if needed
- Monitor Redis memory usage: `redis-cli INFO memory`
- Consider increasing Redis maxmemory if needed

## Benefits
- **Reduced API calls**: Cached responses avoid repeated IGDB API calls
- **Faster response times**: Redis provides sub-millisecond response times
- **Lower costs**: Fewer API calls to IGDB
- **Better user experience**: Pages load significantly faster
