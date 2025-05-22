package org.educandoweb.springweatherdata.controller;

import org.educandoweb.springweatherdata.responses.FavoriteCityResponse;
import org.educandoweb.springweatherdata.responses.ForecastResponse;
import org.educandoweb.springweatherdata.service.FavoriteCityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FavoriteCityControllerTest {

    @InjectMocks
    private FavoriteCityController favoriteCityController;

    @Mock
    private FavoriteCityService favoriteCityService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(userDetails.getUsername()).thenReturn("testuser");
    }

    @Test
    void addFavoriteCity_returnsFavoriteCity() {
        FavoriteCityResponse mockResponse = FavoriteCityResponse.builder().cityName("Berlin").isDefault(true).build();
        when(favoriteCityService.addFavoriteCity("Berlin", true, "testuser")).thenReturn(mockResponse);

        ResponseEntity<FavoriteCityResponse> response = favoriteCityController.addFavoriteCity("Berlin", true, userDetails);

        assertEquals("Berlin", response.getBody().getCityName());
        verify(favoriteCityService).addFavoriteCity("Berlin", true, "testuser");
    }

    @Test
    void getUserFavoriteCities_returnsList() {
        List<FavoriteCityResponse> mockList = List.of(FavoriteCityResponse.builder().cityName("Paris").build());
        when(favoriteCityService.getUserFavoriteCities("testuser")).thenReturn(mockList);

        ResponseEntity<List<FavoriteCityResponse>> response = favoriteCityController.getUserFavoriteCities(userDetails);

        assertEquals(1, response.getBody().size());
        verify(favoriteCityService).getUserFavoriteCities("testuser");
    }

    @Test
    void setDefaultCity_returnsUpdatedCity() {
        FavoriteCityResponse mockResponse = FavoriteCityResponse.builder().cityName("Madrid").isDefault(true).build();
        when(favoriteCityService.setDefaultCity(1L, "testuser")).thenReturn(mockResponse);

        ResponseEntity<FavoriteCityResponse> response = favoriteCityController.setDefaultCity(1L, userDetails);

        assertTrue(response.getBody().getIsDefault());
        verify(favoriteCityService).setDefaultCity(1L, "testuser");
    }

    @Test
    void removeFavoriteCity_noContent() {
        doNothing().when(favoriteCityService).removeFavoriteCity(1L, "testuser");

        ResponseEntity<Void> response = favoriteCityController.removeFavoriteCity(1L, userDetails);

        assertEquals(204, response.getStatusCodeValue());
        verify(favoriteCityService).removeFavoriteCity(1L, "testuser");
    }

    @Test
    void getDefaultCityWeather_returnsForecast() {
        ForecastResponse mockForecast = ForecastResponse.builder().temperature(20.0).build();
        when(favoriteCityService.getDefaultCityWeather("testuser")).thenReturn(mockForecast);

        ResponseEntity<ForecastResponse> response = favoriteCityController.getDefaultCityWeather(userDetails);

        assertEquals(20.0, response.getBody().getTemperature());
        verify(favoriteCityService).getDefaultCityWeather("testuser");
    }

    @Test
    void getDefaultCity_returnsFavoriteCity() {
        FavoriteCityResponse mockResponse = FavoriteCityResponse.builder().cityName("Rome").build();
        when(favoriteCityService.getDefaultCity("testuser")).thenReturn(mockResponse);

        ResponseEntity<FavoriteCityResponse> response = favoriteCityController.getDefaultCity(userDetails);

        assertEquals("Rome", response.getBody().getCityName());
        verify(favoriteCityService).getDefaultCity("testuser");
    }
}
