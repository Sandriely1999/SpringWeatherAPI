package org.educandoweb.springweatherdata.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.educandoweb.springweatherdata.entities.FavoriteCity;
import org.educandoweb.springweatherdata.entities.User;
import org.educandoweb.springweatherdata.repositories.FavoriteCityRepository;
import org.educandoweb.springweatherdata.repositories.UserRepository;
import org.educandoweb.springweatherdata.responses.FavoriteCityResponse;
import org.educandoweb.springweatherdata.responses.ForecastResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteCityService {
    private final FavoriteCityRepository favoriteCityRepository;
    private final UserRepository userRepository;
    private final ForecastService forecastService;

    @Transactional
    public FavoriteCityResponse addFavoriteCity(String cityName, Boolean isDefault, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verificar se a cidade já existe como favorita
        if (favoriteCityRepository.findByUserAndCityName(user, cityName).isPresent()) {
            throw new RuntimeException("City is already in favorites");
        }

        // Se esta cidade for definida como padrão, reset qualquer cidade padrão anterior
        if (Boolean.TRUE.equals(isDefault)) {
            favoriteCityRepository.findByUserAndIsDefaultTrue(user)
                    .ifPresent(defaultCity -> {
                        defaultCity.setIsDefault(false);
                        favoriteCityRepository.save(defaultCity);
                    });
        }

        FavoriteCity favoriteCity = FavoriteCity.builder()
                .user(user)
                .cityName(cityName)
                .isDefault(isDefault)
                .createdAt(LocalDateTime.now())
                .build();

        FavoriteCity savedCity = favoriteCityRepository.save(favoriteCity);

        return mapToResponse(savedCity);
    }

    public List<FavoriteCityResponse> getUserFavoriteCities(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return favoriteCityRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FavoriteCityResponse setDefaultCity(Long cityId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FavoriteCity favoriteCity = favoriteCityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("Favorite city not found"));

        if (!favoriteCity.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to modify this favorite city");
        }

        // Reset cidade padrão anterior
        favoriteCityRepository.findByUserAndIsDefaultTrue(user)
                .ifPresent(defaultCity -> {
                    defaultCity.setIsDefault(false);
                    favoriteCityRepository.save(defaultCity);
                });

        // Definir nova cidade padrão
        favoriteCity.setIsDefault(true);
        FavoriteCity savedCity = favoriteCityRepository.save(favoriteCity);

        return mapToResponse(savedCity);
    }

    public void removeFavoriteCity(Long cityId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FavoriteCity favoriteCity = favoriteCityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("Favorite city not found"));

        if (!favoriteCity.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to remove this favorite city");
        }

        favoriteCityRepository.delete(favoriteCity);
    }

    public ForecastResponse getDefaultCityWeather(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FavoriteCity defaultCity = favoriteCityRepository.findByUserAndIsDefaultTrue(user)
                .orElseThrow(() -> new RuntimeException("No default city set"));

        return forecastService.getCurrentWeather(defaultCity.getCityName());
    }

    private FavoriteCityResponse mapToResponse(FavoriteCity favoriteCity) {
        return FavoriteCityResponse.builder()
                .id(favoriteCity.getId())
                .cityName(favoriteCity.getCityName())
                .isDefault(favoriteCity.getIsDefault())
                .createdAt(favoriteCity.getCreatedAt())
                .build();
    }
}