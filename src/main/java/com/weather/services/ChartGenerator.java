package com.weather.services;

import com.weather.model.WeatherData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class ChartGenerator {

    public String generateTemperatureChart(WeatherData weatherData) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        List<String> times = weatherData.getHourly().getTime();
        List<Double> temperatures = weatherData.getHourly().getTemperature2m();

        // Берем только первые 24 часа
        int hours = Math.min(24, Math.min(times.size(), temperatures.size()));

        for (int i = 0; i < hours; i++) {
            String hour = times.get(i).substring(11, 16); // Извлекаем только время
            dataset.addValue(temperatures.get(i), "Температура", hour);
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Температура в " + weatherData.getCity() + " (24 часа)",
                "Время",
                "Температура (°C)",
                dataset
        );

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ChartUtils.writeChartAsPNG(outputStream, chart, 800, 600);
            byte[] chartBytes = outputStream.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(chartBytes);
        }
    }
}