package org.educandoweb.springweatherdata.controller;

import org.educandoweb.springweatherdata.responses.ForecastResponse;
import org.educandoweb.springweatherdata.service.ForecastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ForecastControllerTest {

    @InjectMocks
    private ForecastController forecastController;

    @Mock
    private ForecastService forecastService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(userDetails.getUsername()).thenReturn("testuser");
    }

    @Test
    void getFiveDayForecast_returnsList() {
        List<ForecastResponse> mockList = List.of(ForecastResponse.builder().temperature(15.0).build());
        when(forecastService.getFiveDayForecast("Berlin")).thenReturn(mockList);

        ResponseEntity<List<ForecastResponse>> response = forecastController.getFiveDayForecast("Berlin", userDetails);

        assertEquals(1, response.getBody().size());
        verify(forecastService).getFiveDayForecast("Berlin");
    }

    @Test
    void getCurrentWeather_returnsForecast() {
        ForecastResponse mockResponse = ForecastResponse.builder().temperature(22.0).build();
        when(forecastService.getCurrentWeather("Berlin")).thenReturn(mockResponse);

        ResponseEntity<ForecastResponse> response = forecastController.getCurrentWeather("Berlin", userDetails);

        assertEquals(22.0, response.getBody().getTemperature());
        verify(forecastService).getCurrentWeather("Berlin");
    }
}
