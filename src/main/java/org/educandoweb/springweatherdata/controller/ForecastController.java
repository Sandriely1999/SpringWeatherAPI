package org.educandoweb.springweatherdata.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.educandoweb.springweatherdata.responses.ForecastResponse;
import org.educandoweb.springweatherdata.service.ForecastService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/weather/forecast")
@Tag(name = "Forecast", description = "Endpoints for weather forecast information")
@RequiredArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;

    @GetMapping("/five-day")
    public ResponseEntity<List<ForecastResponse>> getFiveDayForecast(
            @RequestParam String city,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ForecastResponse> forecast = forecastService.getFiveDayForecast(city);
        return ResponseEntity.ok(forecast);
    }

    @GetMapping("/current")
    public ResponseEntity<ForecastResponse> getCurrentWeather(
            @RequestParam String city,
            @AuthenticationPrincipal UserDetails userDetails) {
        ForecastResponse weather = forecastService.getCurrentWeather(city);
        return ResponseEntity.ok(weather);
    }


}