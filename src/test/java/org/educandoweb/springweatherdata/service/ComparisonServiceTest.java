package org.educandoweb.springweatherdata.service;

import org.educandoweb.springweatherdata.responses.ForecastResponse;
import org.educandoweb.springweatherdata.responses.WeatherComparisonResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComparisonServiceTest {

    @InjectMocks
    private ComparisonService comparisonService;

    @Mock
    private ForecastService forecastService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void compareWeather_returnsComparisons() {
        ForecastResponse forecast1 = ForecastResponse.builder()
                .temperature(20.0)
                .humidity(50)
                .description("clear")
                .build();

        ForecastResponse forecast2 = ForecastResponse.builder()
                .temperature(15.0)
                .humidity(55)
                .description("cloudy")
                .build();

        when(forecastService.getCurrentWeather("CityA")).thenReturn(forecast1);
        when(forecastService.getCurrentWeather("CityB")).thenReturn(forecast2);

        List<WeatherComparisonResponse> results = comparisonService.compareWeather(List.of("CityA", "CityB"));

        assertEquals(1, results.size());
        WeatherComparisonResponse comparison = results.get(0);


        assertEquals("CityA", comparison.getCityA());
        assertEquals("CityB", comparison.getCityB());
        assertEquals(20.0, comparison.getTemperatureA());
        assertEquals(15.0, comparison.getTemperatureB());
        assertEquals(5.0, comparison.getTemperatureDifference());
        assertEquals(50, comparison.getHumidityA());
        assertEquals(55, comparison.getHumidityB());
        assertEquals(-5, comparison.getHumidityDifference());
    }
}
