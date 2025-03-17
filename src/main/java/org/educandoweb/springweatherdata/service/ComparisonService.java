package org.educandoweb.springweatherdata.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.educandoweb.springweatherdata.responses.WeatherComparisonResponse;
import org.educandoweb.springweatherdata.responses.WeatherSearchResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComparisonService {
    private final WeatherService weatherService;

    public List<WeatherComparisonResponse> compareWeather(List<String> cities, String username) {
        List<WeatherComparisonResponse> comparisons = new ArrayList<>();
        Map<String, WeatherSearchResponse> cityWeatherMap = new HashMap<>();

        // Obter dados climáticos para cada cidade
        for (String city : cities) {
            try {
                WeatherSearchResponse weatherData = weatherService.getCurrentWeather(city, username);
                cityWeatherMap.put(city, weatherData);
            } catch (Exception e) {
                log.error("Error fetching weather data for city {}: {}", city, e.getMessage());
            }
        }

        // Calcular diferenças e criar comparações para cada par de cidades
        List<String> processedCities = new ArrayList<>(cityWeatherMap.keySet());

        for (int i = 0; i < processedCities.size(); i++) {
            String cityA = processedCities.get(i);
            WeatherSearchResponse weatherA = cityWeatherMap.get(cityA);

            for (int j = i + 1; j < processedCities.size(); j++) {
                String cityB = processedCities.get(j);
                WeatherSearchResponse weatherB = cityWeatherMap.get(cityB);

                // Calcular diferenças
                double tempDiff = weatherA.getTemperature() - weatherB.getTemperature();
                int humidityDiff = weatherA.getHumidity() - weatherB.getHumidity();

                // Criar resposta de comparação
                WeatherComparisonResponse comparison = WeatherComparisonResponse.builder()
                        .cityA(cityA)
                        .cityB(cityB)
                        .temperatureA(weatherA.getTemperature())
                        .temperatureB(weatherB.getTemperature())
                        .temperatureDifference(tempDiff)
                        .humidityA(weatherA.getHumidity())
                        .humidityB(weatherB.getHumidity())
                        .humidityDifference(humidityDiff)
                        .descriptionA(weatherA.getDescription())
                        .descriptionB(weatherB.getDescription())
                        .comparisonDate(weatherA.getSearchDate())
                        .build();

                comparisons.add(comparison);
            }
        }

        return comparisons;
    }
}