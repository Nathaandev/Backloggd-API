package com.example.backloggd.Config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringJUnitConfig
class CacheConfigTest {

    @Test
    @ActiveProfiles("redis")
    void redisProfile_createsRedisCacheManager() {
        CacheConfig config = new CacheConfig();
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        
        CacheManager cacheManager = config.redisCacheManager(connectionFactory);
        
        assertNotNull(cacheManager);
        assertTrue(cacheManager instanceof RedisCacheManager);
    }

    @Test
    @ActiveProfiles("!redis")
    void nonRedisProfile_createsSimpleCacheManager() {
        CacheConfig config = new CacheConfig();
        
        CacheManager cacheManager = config.simpleCacheManager();
        
        assertNotNull(cacheManager);
        assertTrue(cacheManager instanceof ConcurrentMapCacheManager);
        
        ConcurrentMapCacheManager simpleManager = (ConcurrentMapCacheManager) cacheManager;
        assertTrue(simpleManager.getCacheNames().contains("games"));
        assertTrue(simpleManager.getCacheNames().contains("gameDetails"));
        assertTrue(simpleManager.getCacheNames().contains("gamesByGenre"));
        assertTrue(simpleManager.getCacheNames().contains("gamesByDeveloper"));
        assertTrue(simpleManager.getCacheNames().contains("gamesByPublisher"));
        assertTrue(simpleManager.getCacheNames().contains("gamesByMetacritic"));
        assertTrue(simpleManager.getCacheNames().contains("gamesByTags"));
    }
}
