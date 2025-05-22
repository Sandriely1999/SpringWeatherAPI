package org.educandoweb.springweatherdata.controller;

import org.educandoweb.springweatherdata.responses.WeatherComparisonResponse;
import org.educandoweb.springweatherdata.service.ComparisonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComparisonControllerTest {

    @InjectMocks
    private ComparisonController comparisonController;

    @Mock
    private ComparisonService comparisonService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(userDetails.getUsername()).thenReturn("testuser");
    }

    @Test
    void compareWeather_returnsComparisons() {
        List<String> cities = List.of("Paris", "London");
        List<WeatherComparisonResponse> mockResponse = List.of(new WeatherComparisonResponse());

        when(comparisonService.compareWeather(cities)).thenReturn(mockResponse);

        ResponseEntity<List<WeatherComparisonResponse>> response = comparisonController.compareWeather(cities, userDetails);

        assertEquals(1, response.getBody().size());
        verify(comparisonService).compareWeather(cities);
    }

    @Test
    void compareWeather_throwsExceptionWhenLessThanTwoCities() {
        List<String> cities = List.of("Paris");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> comparisonController.compareWeather(cities, userDetails));
        assertEquals("At least two cities must be provided for comparison", thrown.getMessage());
    }
}
