package com.weather.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.model.Coordinates;
import com.weather.model.GeocodingResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class GeoCodingService {
    private static final String GEOCODING_API_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeoCodingService() {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public Coordinates getCoordinates(String city) throws IOException {
        String url = String.format("%s?name=%s", GEOCODING_API_URL, city);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Geocoding API request failed: " + response.code());
            }

            String responseBody = response.body().string();
            GeocodingResponse geocodingResponse = objectMapper.readValue(responseBody, GeocodingResponse.class);

            if (geocodingResponse.getResults() == null || geocodingResponse.getResults().isEmpty()) {
                throw new IOException("City not found: " + city);
            }

            var firstResult = geocodingResponse.getResults().getFirst();
            return new Coordinates(firstResult.getLatitude(), firstResult.getLongitude());
        }
    }
}
