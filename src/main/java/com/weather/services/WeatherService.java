package com.weather.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.model.WeatherData;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Locale;

@RequiredArgsConstructor
public class WeatherService {
    private static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast";
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WeatherService() {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public WeatherData getWeatherData(double latitude, double longitude, String city) throws IOException {
        String url = String.format(Locale.US, "%s?latitude=%.6f&longitude=%.6f&hourly=temperature_2m",
                WEATHER_API_URL, latitude, longitude);

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Weather API request failed: " + response.code());
            }

            String responseBody = response.body().string();
            WeatherData weatherData = objectMapper.readValue(responseBody, WeatherData.class);
            weatherData.setTimestamp(System.currentTimeMillis());
            weatherData.setCity(city);

            return weatherData;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
