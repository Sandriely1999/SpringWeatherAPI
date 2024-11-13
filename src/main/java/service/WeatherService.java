package service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.entities.User;
import model.entities.WeatherSearch;
import model.responses.WeatherSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import repositories.UserRepository;
import repositories.WeatherSearchRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {
    private final RestTemplate restTemplate;
    private final WeatherSearchRepository weatherSearchRepository;
    private final UserRepository userRepository;

    @Value("${weather.api.url}")
    private String apiUrl;

    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherSearchResponse getCurrentWeather(String cityName, String username) {
        log.info("Fetching weather data for city: {}", cityName);
        String url = String.format("%sweather?q=%s&appid=%s&units=metric&lang=pt_br",
                apiUrl, cityName, apiKey);

        try {
            // Criando uma classe interna para mapear a resposta da API
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            return saveWeatherSearch(cityName, response, username);
        } catch (Exception e) {
            log.error("Error fetching weather data: {}", e.getMessage());
            throw new RuntimeException("Error fetching weather data", e);
        }
    }

    private WeatherSearchResponse saveWeatherSearch(String cityName, JsonNode weatherData, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Extraindo dados do JsonNode
        Double temperature = weatherData.path("main").path("temp").asDouble();
        Integer humidity = weatherData.path("main").path("humidity").asInt();
        String description = weatherData.path("weather").get(0).path("description").asText();

        WeatherSearch search = WeatherSearch.builder()
                .user(user)
                .city(cityName)
                .temperature(temperature)
                .humidity(humidity)
                .description(description)
                .searchDate(LocalDateTime.now())
                .build();

        WeatherSearch savedSearch = weatherSearchRepository.save(search);

        return WeatherSearchResponse.builder()
                .city(savedSearch.getCity())
                .temperature(savedSearch.getTemperature())
                .humidity(savedSearch.getHumidity())
                .description(savedSearch.getDescription())
                .searchDate(savedSearch.getSearchDate())
                .build();
    }

    public List<WeatherSearchResponse> getWeatherSearches(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return weatherSearchRepository.findByUserOrderBySearchDateDesc(user)
                .stream()
                .map(search -> WeatherSearchResponse.builder()
                        .city(search.getCity())
                        .temperature(search.getTemperature())
                        .humidity(search.getHumidity())
                        .description(search.getDescription())
                        .searchDate(search.getSearchDate())
                        .build())
                .collect(Collectors.toList());
    }

    public Map<String, Object> getStatistics(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<WeatherSearch> searches = weatherSearchRepository.findByUserOrderBySearchDateDesc(user);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalSearches", searches.size());
        statistics.put("mostSearchedCity", getMostSearchedCity(searches));
        statistics.put("averageTemperature", getAverageTemperature(searches));

        return statistics;
    }

    private String getMostSearchedCity(List<WeatherSearch> searches) {
        return searches.stream()
                .collect(Collectors.groupingBy(WeatherSearch::getCity, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    private Double getAverageTemperature(List<WeatherSearch> searches) {
        return searches.stream()
                .mapToDouble(WeatherSearch::getTemperature)
                .average()
                .orElse(0.0);
    }
}