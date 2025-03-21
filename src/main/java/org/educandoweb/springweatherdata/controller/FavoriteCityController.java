package org.educandoweb.springweatherdata.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.educandoweb.springweatherdata.responses.FavoriteCityResponse;
import org.educandoweb.springweatherdata.responses.ForecastResponse;
import org.educandoweb.springweatherdata.service.FavoriteCityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weather/favorites")
@Tag(name = "Favorite Cities", description = "Endpoints for managing favorite cities")
@RequiredArgsConstructor
public class FavoriteCityController {

    private final FavoriteCityService favoriteCityService;

    @PostMapping
    public ResponseEntity<FavoriteCityResponse> addFavoriteCity(
            @RequestParam String cityName,
            @RequestParam(required = false, defaultValue = "false") Boolean isDefault,
            @AuthenticationPrincipal UserDetails userDetails) {
        FavoriteCityResponse favoriteCity = favoriteCityService.addFavoriteCity(cityName, isDefault, userDetails.getUsername());
        return ResponseEntity.ok(favoriteCity);
    }

    @GetMapping
    public ResponseEntity<List<FavoriteCityResponse>> getUserFavoriteCities(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<FavoriteCityResponse> favoriteCities = favoriteCityService.getUserFavoriteCities(userDetails.getUsername());
        return ResponseEntity.ok(favoriteCities);
    }

    @PutMapping("/{cityId}/default")
    public ResponseEntity<FavoriteCityResponse> setDefaultCity(
            @PathVariable Long cityId,
            @AuthenticationPrincipal UserDetails userDetails) {
        FavoriteCityResponse favoriteCity = favoriteCityService.setDefaultCity(cityId, userDetails.getUsername());
        return ResponseEntity.ok(favoriteCity);
    }

    @DeleteMapping("/{cityId}")
    public ResponseEntity<Void> removeFavoriteCity(
            @PathVariable Long cityId,
            @AuthenticationPrincipal UserDetails userDetails) {
        favoriteCityService.removeFavoriteCity(cityId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/default/weather")
    public ResponseEntity<ForecastResponse> getDefaultCityWeather(
            @AuthenticationPrincipal UserDetails userDetails) {
       ForecastResponse weather = favoriteCityService.getDefaultCityWeather(userDetails.getUsername());
        return ResponseEntity.ok(weather);
    }
}