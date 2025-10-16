package com.weather.server.response;

import com.weather.model.WeatherData;

public record WeatherResponse (
        WeatherData weather,
        String chart,
        boolean fromCache
) {
}
