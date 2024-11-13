package controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import model.responses.WeatherSearchResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.WeatherService;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/weather")
@Tag(name = "Weather", description = "Endpoints for weather information")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/current")
    public ResponseEntity<WeatherSearchResponse> getCurrentWeather(
            @RequestParam String city,
            @AuthenticationPrincipal UserDetails userDetails) {
        WeatherSearchResponse weather = weatherService.getCurrentWeather(city, userDetails.getUsername());
        return ResponseEntity.ok(weather);
    }

    @GetMapping("/history")
    public ResponseEntity<List<WeatherSearchResponse>> getSearchHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<WeatherSearchResponse> weatherList = weatherService.getWeatherSearches(userDetails.getUsername());
        return ResponseEntity.ok(weatherList);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> statistics = weatherService.getStatistics(userDetails.getUsername());
        return ResponseEntity.ok(statistics);
    }
}