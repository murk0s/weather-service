package com.weather.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.model.WeatherData;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@RequiredArgsConstructor
public class CacheService {
    private static final int CACHE_TTL_SECONDS = 900; // 15 минут
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;

    public CacheService() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        this.jedisPool = new JedisPool(poolConfig, "localhost", 6379);
        this.objectMapper = new ObjectMapper();
    }

    public void cacheWeatherData(String city, WeatherData weatherData) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "weather:" + city.toLowerCase();
            String value = objectMapper.writeValueAsString(weatherData);
            jedis.setex(key, CACHE_TTL_SECONDS, value);
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing weather data: " + e.getMessage());
        }
    }

    public WeatherData getCachedWeatherData(String city) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "weather:" + city.toLowerCase();
            String value = jedis.get(key);

            if (value != null) {
                return objectMapper.readValue(value, WeatherData.class);
            }
        } catch (Exception e) {
            System.err.println("Error reading from cache: " + e.getMessage());
        }
        return null;
    }
}
