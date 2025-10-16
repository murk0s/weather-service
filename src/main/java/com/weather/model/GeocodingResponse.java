package com.weather.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeocodingResponse {
    @JsonProperty("results")
    private List<GeocodingResult> results;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class GeocodingResult {
        @JsonProperty("name")
        private String name;

        @JsonProperty("latitude")
        private double latitude;

        @JsonProperty("longitude")
        private double longitude;

        @JsonProperty("country")
        private String country;
    }
}
