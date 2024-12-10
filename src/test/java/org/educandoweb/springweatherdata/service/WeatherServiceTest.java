package org.educandoweb.springweatherdata.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.educandoweb.springweatherdata.entities.User;
import org.educandoweb.springweatherdata.entities.WeatherSearch;
import org.educandoweb.springweatherdata.repositories.UserRepository;
import org.educandoweb.springweatherdata.repositories.WeatherSearchRepository;
import org.educandoweb.springweatherdata.responses.WeatherSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private WeatherSearchRepository weatherSearchRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WeatherService weatherService;

    private User testUser;
    private WeatherSearch testWeatherSearch;
    private JsonNode mockWeatherData;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(weatherService, "apiUrl", "http://test.api/");
        ReflectionTestUtils.setField(weatherService, "apiKey", "test-key");


        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();


        testWeatherSearch = WeatherSearch.builder()
                .id(1L)
                .user(testUser)
                .city("London")
                .temperature(20.0)
                .humidity(70)
                .description("Clear sky")
                .searchDate(LocalDateTime.now())
                .build();


        ObjectMapper mapper = new ObjectMapper();
        ObjectNode mainNode = mapper.createObjectNode();
        mainNode.put("temp", 20.0);
        mainNode.put("humidity", 70);

        ObjectNode weatherDesc = mapper.createObjectNode();
        weatherDesc.put("description", "Clear sky");

        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.set("main", mainNode);
        rootNode.set("weather", mapper.createArrayNode().add(weatherDesc));

        mockWeatherData = rootNode;
    }

    @Test
    void getCurrentWeather_ShouldReturnWeatherData() {

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(restTemplate.getForObject(anyString(), any())).thenReturn(mockWeatherData);
        when(weatherSearchRepository.save(any(WeatherSearch.class))).thenReturn(testWeatherSearch);


        WeatherSearchResponse response = weatherService.getCurrentWeather("London", "testuser");


        assertNotNull(response);
        assertEquals("London", response.getCity());
        assertEquals(20.0, response.getTemperature());
        assertEquals(70, response.getHumidity());
        assertEquals("Clear sky", response.getDescription());
    }

    @Test
    void getWeatherSearches_ShouldReturnSearchHistory() {

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(weatherSearchRepository.findByUserOrderBySearchDateDesc(any(User.class)))
                .thenReturn(Arrays.asList(testWeatherSearch));


        List<WeatherSearchResponse> responses = weatherService.getWeatherSearches("testuser");


        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals("London", responses.get(0).getCity());
    }

    @Test
    void getStatistics_ShouldReturnCorrectStats() {

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(weatherSearchRepository.findByUserOrderBySearchDateDesc(any(User.class)))
                .thenReturn(Arrays.asList(testWeatherSearch));


        Map<String, Object> statistics = weatherService.getStatistics("testuser");


        assertNotNull(statistics);
        assertEquals(1, statistics.get("totalSearches"));
        assertEquals("London", statistics.get("mostSearchedCity"));
        assertEquals(20.0, statistics.get("averageTemperature"));
    }
}