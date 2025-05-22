package org.educandoweb.springweatherdata.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.educandoweb.springweatherdata.repositories.UserRepository;
import org.educandoweb.springweatherdata.responses.ForecastResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ForecastServiceTest {

    private ForecastService forecastService;
    private RestTemplate restTemplate;
    private UserRepository userRepository;

    private final String fakeApiUrl = "https://api.openweathermap.org/data/2.5/";
    private final String fakeApiKey = "fakeapikey";

    @BeforeEach
    void setup() {
        restTemplate = mock(RestTemplate.class);
        userRepository = mock(UserRepository.class);

        forecastService = new ForecastService(restTemplate, userRepository);
        // Configurar os valores manualmente já que não temos um contexto Spring para @Value
        forecastService.getClass().getDeclaredFields();
        try {
            var apiUrlField = ForecastService.class.getDeclaredField("apiUrl");
            apiUrlField.setAccessible(true);
            apiUrlField.set(forecastService, fakeApiUrl);

            var apiKeyField = ForecastService.class.getDeclaredField("apiKey");
            apiKeyField.setAccessible(true);
            apiKeyField.set(forecastService, fakeApiKey);
        } catch (Exception e) {
            fail("Erro ao configurar campos via reflection");
        }
    }

    @Test
    void testGetCurrentWeather_success() throws Exception {
        String cityName = "Sao Paulo";

        // Simula a resposta da API
        String json = """
        {
            "main": {
                "temp": 25.0,
                "humidity": 70
            },
            "weather": [
                {
                    "description": "céu limpo"
                }
            ],
            "dt": 1710000000
        }
        """;
        JsonNode mockedResponse = new ObjectMapper().readTree(json);

        String expectedUrl = String.format("%sweather?q=%s&appid=%s&units=metric&lang=pt_br",
                fakeApiUrl, cityName, fakeApiKey);

        when(restTemplate.getForObject(eq(expectedUrl), eq(JsonNode.class)))
                .thenReturn(mockedResponse);

        ForecastResponse response = forecastService.getCurrentWeather(cityName);

        assertEquals("Sao Paulo", response.getCity());
        assertEquals(25.0, response.getTemperature());
        assertEquals(70, response.getHumidity());
        assertEquals("céu limpo", response.getDescription());
        assertNotNull(response.getForecastDate());
    }

    @Test
    void testGetFiveDayForecast_success() throws Exception {
        String cityName = "Sao Paulo";

        String json = """
        {
          "list": [
            {
              "dt": 1710000000,
              "main": {
                "temp": 22.5,
                "humidity": 60
              },
              "weather": [
                {
                  "description": "parcialmente nublado"
                }
              ]
            }
          ]
        }
        """;

        JsonNode mockedResponse = new ObjectMapper().readTree(json);

        String expectedUrl = String.format("%sforecast?q=%s&appid=%s&units=metric&lang=pt_br",
                fakeApiUrl, cityName, fakeApiKey);

        when(restTemplate.getForObject(eq(expectedUrl), eq(JsonNode.class)))
                .thenReturn(mockedResponse);

        List<ForecastResponse> responses = forecastService.getFiveDayForecast(cityName);
        assertEquals(1, responses.size());

        ForecastResponse forecast = responses.get(0);
        assertEquals("Sao Paulo", forecast.getCity());
        assertEquals(22.5, forecast.getTemperature());
        assertEquals(60, forecast.getHumidity());
        assertEquals("parcialmente nublado", forecast.getDescription());
    }

    @Test
    void testGetCurrentWeather_error() {
        String cityName = "CidadeInvalida";
        String expectedUrl = String.format("%sweather?q=%s&appid=%s&units=metric&lang=pt_br",
                fakeApiUrl, cityName, fakeApiKey);

        when(restTemplate.getForObject(eq(expectedUrl), eq(JsonNode.class)))
                .thenThrow(new RuntimeException("API fora do ar"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            forecastService.getCurrentWeather(cityName);
        });

        assertTrue(ex.getMessage().contains("Error fetching weather data"));
    }

    @Test
    void testGetFiveDayForecast_error() {
        String cityName = "CidadeInvalida";
        String expectedUrl = String.format("%sforecast?q=%s&appid=%s&units=metric&lang=pt_br",
                fakeApiUrl, cityName, fakeApiKey);

        when(restTemplate.getForObject(eq(expectedUrl), eq(JsonNode.class)))
                .thenThrow(new RuntimeException("Erro na API"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            forecastService.getFiveDayForecast(cityName);
        });

        assertTrue(ex.getMessage().contains("Error fetching forecast data"));
    }
}
