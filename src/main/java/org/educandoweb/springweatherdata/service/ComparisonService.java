package org.educandoweb.springweatherdata.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.educandoweb.springweatherdata.repositories.UserRepository;
import org.educandoweb.springweatherdata.responses.ForecastResponse;
import org.educandoweb.springweatherdata.responses.WeatherComparisonResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComparisonService {
    private final UserRepository userRepository;
    private final ForecastService forecastService;

    public List<WeatherComparisonResponse> compareWeather(List<String> cities) {
        List<WeatherComparisonResponse> comparisons = new ArrayList<>();
        Map<String, ForecastResponse> cityWeatherMap = new HashMap<>();

        // Obter dados climáticos para cada cidade
        for (String city : cities) {
            try {
                ForecastResponse weatherData = forecastService.getCurrentWeather(city);
                cityWeatherMap.put(city, weatherData);
            } catch (Exception e) {
                log.error("Error fetching weather data for city {}: {}", city, e.getMessage());
            }
        }

        // Calcular diferenças e criar comparações para cada par de cidades
        List<String> processedCities = new ArrayList<>(cityWeatherMap.keySet());

        for (int i = 0; i < processedCities.size(); i++) {
            String cityA = processedCities.get(i);
            ForecastResponse weatherA = cityWeatherMap.get(cityA);

            for (int j = i + 1; j < processedCities.size(); j++) {
                String cityB = processedCities.get(j);
                ForecastResponse weatherB = cityWeatherMap.get(cityB);

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
                        .comparisonDate(LocalDateTime.now()) // Adicionado
                        .build();

                comparisons.add(comparison);
            }
        }

        return comparisons;
    }


}