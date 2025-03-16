package org.educandoweb.springweatherdata.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.educandoweb.springweatherdata.entities.User;
import org.educandoweb.springweatherdata.entities.WeatherForecast;
import org.educandoweb.springweatherdata.repositories.UserRepository;
import org.educandoweb.springweatherdata.repositories.WeatherForecastRepository;
import org.educandoweb.springweatherdata.responses.ForecastResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ForecastService {
    private final RestTemplate restTemplate;
    private final WeatherForecastRepository forecastRepository;
    private final UserRepository userRepository;

    @Value("${weather.api.url}")
    private String apiUrl;

    @Value("${weather.api.key}")
    private String apiKey;

    public ForecastService(RestTemplate restTemplate, WeatherForecastRepository forecastRepository, UserRepository userRepository) {
        this.restTemplate = restTemplate;
        this.forecastRepository = forecastRepository;
        this.userRepository = userRepository;
    }

    public List<ForecastResponse> getFiveDayForecast(String cityName, String username) {
        log.info("Fetching 5-day forecast data for city: {}", cityName);
        String url = String.format("%sforecast?q=%s&appid=%s&units=metric&lang=pt_br",
                apiUrl, cityName, apiKey);

        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            return saveForecastData(cityName, response, username);
        } catch (Exception e) {
            log.error("Error fetching forecast data: {}", e.getMessage());
            throw new RuntimeException("Error fetching forecast data", e);
        }
    }

    private List<ForecastResponse> saveForecastData(String cityName, JsonNode forecastData, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ForecastResponse> forecastResponses = new ArrayList<>();
        JsonNode list = forecastData.path("list");

        for (JsonNode forecast : list) {
            long dt = forecast.path("dt").asLong();
            LocalDateTime forecastDate = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(dt), ZoneId.systemDefault());

            Double temperature = forecast.path("main").path("temp").asDouble();
            Integer humidity = forecast.path("main").path("humidity").asInt();
            String description = forecast.path("weather").get(0).path("description").asText();

            WeatherForecast weatherForecast = WeatherForecast.builder()
                    .user(user)
                    .city(cityName)
                    .temperature(temperature)
                    .humidity(humidity)
                    .description(description)
                    .forecastDate(forecastDate)
                    .createdAt(LocalDateTime.now())
                    .build();

            WeatherForecast savedForecast = forecastRepository.save(weatherForecast);

            forecastResponses.add(ForecastResponse.builder()
                    .city(savedForecast.getCity())
                    .temperature(savedForecast.getTemperature())
                    .humidity(savedForecast.getHumidity())
                    .description(savedForecast.getDescription())
                    .forecastDate(savedForecast.getForecastDate())
                    .build());
        }

        return forecastResponses;
    }

    public List<ForecastResponse> getForecastsByCity(String cityName, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return forecastRepository.findByUserAndCityOrderByForecastDateAsc(user, cityName)
                .stream()
                .map(forecast -> ForecastResponse.builder()
                        .city(forecast.getCity())
                        .temperature(forecast.getTemperature())
                        .humidity(forecast.getHumidity())
                        .description(forecast.getDescription())
                        .forecastDate(forecast.getForecastDate())
                        .build())
                .collect(Collectors.toList());
    }
}