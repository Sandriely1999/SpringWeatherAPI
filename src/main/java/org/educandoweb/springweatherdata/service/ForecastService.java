package org.educandoweb.springweatherdata.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.educandoweb.springweatherdata.repositories.UserRepository;
import org.educandoweb.springweatherdata.responses.ForecastResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ForecastService {
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    @Value("${weather.api.url}")
    private String apiUrl;

    @Value("${weather.api.key}")
    private String apiKey;

    public ForecastService(RestTemplate restTemplate, UserRepository userRepository) {
        this.restTemplate = restTemplate;

        this.userRepository = userRepository;

    }

    public List<ForecastResponse> getFiveDayForecast(String cityName) {
        log.info("Fetching 5-day forecast data for city: {}", cityName);
        String url = String.format("%sforecast?q=%s&appid=%s&units=metric&lang=pt_br",
                apiUrl, cityName, apiKey);

        try {

            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            return translateForecastListJsonToResponse(cityName, response);
        } catch (Exception e) {
            log.error("Error fetching forecast data: {}", e.getMessage());
            throw new RuntimeException("Error fetching forecast data", e);
        }
    }

    private List<ForecastResponse> translateForecastListJsonToResponse(String cityName, JsonNode forecastData) {
        List<ForecastResponse> forecastResponses = new ArrayList<>();
        JsonNode list = forecastData.path("list");

        for (JsonNode forecast : list) {
            long dt = forecast.path("dt").asLong();
            LocalDateTime forecastDate = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(dt), ZoneId.systemDefault());

            Double temperature = forecast.path("main").path("temp").asDouble();
            Integer humidity = forecast.path("main").path("humidity").asInt();
            String description = forecast.path("weather").get(0).path("description").asText();


            forecastResponses.add(ForecastResponse.builder()
                    .city(cityName)
                    .temperature(temperature)
                    .humidity(humidity)
                    .description(description)
                    .forecastDate(forecastDate)
                    .build());
        }

        return forecastResponses;
    }


    public ForecastResponse getCurrentWeather(String cityName) {
        log.info("Fetching weather data for city: {}", cityName);
        String url = String.format("%sweather?q=%s&appid=%s&units=metric&lang=pt_br",
                apiUrl, cityName, apiKey);

        try {
            // Criando uma classe interna para mapear a resposta da API
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            return translateForecastJsonToResponse(cityName, response);
        } catch (Exception e) {
            log.error("Error fetching weather data: {}", e.getMessage());
            throw new RuntimeException("Error fetching weather data", e);
        }
    }

    private ForecastResponse translateForecastJsonToResponse(String cityName, JsonNode response) {
        Double temperature = response.path("main").path("temp").asDouble();
        Integer humidity = response.path("main").path("humidity").asInt();
        String description = response.path("weather").get(0).path("description").asText();

        // Adicione esta parte para pegar a data atual quando não houver forecastDate
        long dt = response.path("dt").asLong();
        LocalDateTime forecastDate = (dt != 0) ?
                LocalDateTime.ofInstant(Instant.ofEpochSecond(dt), ZoneId.systemDefault()) :
                LocalDateTime.now(); // Data atual como fallback

        return ForecastResponse.builder()
                .city(cityName)
                .temperature(temperature)
                .humidity(humidity)
                .description(description)
                .forecastDate(forecastDate) // Agora nunca será nulo
                .build();
    }




}