package com.weather.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.model.WeatherData;
import com.weather.server.response.ErrorResponse;
import com.weather.server.response.WeatherResponse;
import com.weather.services.CacheService;
import com.weather.services.ChartGenerator;
import com.weather.services.GeoCodingService;
import com.weather.services.WeatherService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class WeatherHandler {
    private final GeoCodingService geoCodingService;
    private final WeatherService weatherService;
    private final CacheService cacheService;
    private final ChartGenerator chartGenerator;
    private final ObjectMapper objectMapper;

    public WeatherHandler() {
        this.geoCodingService = new GeoCodingService();
        this.weatherService = new WeatherService();
        this.cacheService = new CacheService();
        this.chartGenerator = new ChartGenerator();
        this.objectMapper = new ObjectMapper();
    }

    public void handleWeatherRequest(String query, OutputStream outputStream) throws IOException {
        String city = extractCityFromQuery(query);

        if (city == null || city.isEmpty()) {
            sendErrorResponse(outputStream, "City parameter is required");
            return;
        }

        try {
            WeatherData weatherData = cacheService.getCachedWeatherData(city);
            boolean fromCache = true;

            if (weatherData == null) {
                fromCache = false;
                var coordinates = geoCodingService.getCoordinates(city);
                weatherData = weatherService.getWeatherData(
                        coordinates.getLatitude(),
                        coordinates.getLongitude(),
                        city
                );
                cacheService.cacheWeatherData(city, weatherData);
            }

            String chartBase64 = chartGenerator.generateTemperatureChart(weatherData);

            WeatherResponse response = new WeatherResponse(weatherData, chartBase64, fromCache);
            sendJsonResponse(outputStream, response);

        } catch (IOException e) {
            sendErrorResponse(outputStream, "Error fetching weather data: " + e.getMessage());
        }
    }

    private String extractCityFromQuery(String query) {
        if (query == null) return null;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2 && "city".equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        return null;
    }

    private void sendJsonResponse(OutputStream outputStream, Object data) throws IOException {
        String json = objectMapper.writeValueAsString(data);
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + json.length() + "\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "\r\n" +
                json;
        outputStream.write(response.getBytes(StandardCharsets.UTF_8));
    }

    private void sendErrorResponse(OutputStream outputStream, String message) throws IOException {
        ErrorResponse error = new ErrorResponse(message);
        String json = objectMapper.writeValueAsString(error);
        String response = "HTTP/1.1 400 Bad Request\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + json.length() + "\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "\r\n" +
                json;
        outputStream.write(response.getBytes(StandardCharsets.UTF_8));
    }
}