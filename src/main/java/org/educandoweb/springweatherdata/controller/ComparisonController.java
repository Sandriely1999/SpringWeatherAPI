package org.educandoweb.springweatherdata.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.educandoweb.springweatherdata.responses.WeatherComparisonResponse;
import org.educandoweb.springweatherdata.service.ComparisonService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/weather/compare")
@Tag(name = "Weather Comparison", description = "Endpoints for comparing weather between cities")
@RequiredArgsConstructor
public class ComparisonController {

    private final ComparisonService comparisonService;

    @GetMapping
    public ResponseEntity<List<WeatherComparisonResponse>> compareWeather(
            @RequestParam List<String> cities,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (cities.size() < 2) {
            throw new IllegalArgumentException("At least two cities must be provided for comparison");
        }

        List<WeatherComparisonResponse> comparisons = comparisonService.compareWeather(cities);
        return ResponseEntity.ok(comparisons);
    }
}