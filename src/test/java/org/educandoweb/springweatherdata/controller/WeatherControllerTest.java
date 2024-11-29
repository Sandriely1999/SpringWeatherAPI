package org.educandoweb.springweatherdata.controller;

import org.educandoweb.springweatherdata.responses.WeatherSearchResponse;
import org.educandoweb.springweatherdata.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherControllerTest {

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private WeatherController weatherController;

    private UserDetails userDetails;
    private WeatherSearchResponse weatherResponse;

    @BeforeEach
    void setUp() {
        userDetails = User.withUsername("testuser")
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        weatherResponse = WeatherSearchResponse.builder()
                .city("London")
                .temperature(20.0)
                .humidity(70)
                .description("Clear sky")
                .searchDate(LocalDateTime.now())
                .build();
    }

    @Test
    void getCurrentWeather_Success() {
        // Arrange
        when(weatherService.getCurrentWeather(anyString(), anyString()))
                .thenReturn(weatherResponse);

        // Act
        ResponseEntity<WeatherSearchResponse> response =
                weatherController.getCurrentWeather("London", userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("London", response.getBody().getCity());
    }

    @Test
    void getSearchHistory_Success() {
        // Arrange
        List<WeatherSearchResponse> weatherList = Arrays.asList(weatherResponse);
        when(weatherService.getWeatherSearches(anyString()))
                .thenReturn(weatherList);

        // Act
        ResponseEntity<List<WeatherSearchResponse>> response =
                weatherController.getSearchHistory(userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertFalse(response.getBody().isEmpty());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getStatistics_Success() {
        // Arrange
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalSearches", 10);
        statistics.put("mostSearchedCity", "London");
        statistics.put("averageTemperature", 22.5);

        when(weatherService.getStatistics(anyString())).thenReturn(statistics);

        // Act
        ResponseEntity<Map<String, Object>> response =
                weatherController.getStatistics(userDetails);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(10, response.getBody().get("totalSearches"));
        assertEquals("London", response.getBody().get("mostSearchedCity"));
    }
}