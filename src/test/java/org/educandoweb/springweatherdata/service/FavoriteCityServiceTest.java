package org.educandoweb.springweatherdata.service;

import jakarta.persistence.EntityNotFoundException;
import org.educandoweb.springweatherdata.entities.FavoriteCity;
import org.educandoweb.springweatherdata.entities.User;
import org.educandoweb.springweatherdata.repositories.FavoriteCityRepository;
import org.educandoweb.springweatherdata.repositories.UserRepository;
import org.educandoweb.springweatherdata.responses.FavoriteCityResponse;
import org.educandoweb.springweatherdata.responses.ForecastResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FavoriteCityServiceTest {

    @InjectMocks
    private FavoriteCityService favoriteCityService;

    @Mock
    private FavoriteCityRepository favoriteCityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ForecastService forecastService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = User.builder().id(1L).username("testuser").build();
    }

    @Test
    void addFavoriteCity_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(favoriteCityRepository.findByUserAndCityName(testUser, "Berlin")).thenReturn(Optional.empty());
        when(favoriteCityRepository.findByUserAndIsDefaultTrue(testUser)).thenReturn(Optional.empty());

        // O serviço cria o objeto com LocalDateTime.now(), então não precisa mockar aqui o createdAt
        FavoriteCity savedCity = FavoriteCity.builder()
                .id(1L)
                .user(testUser)
                .cityName("Berlin")
                .isDefault(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(favoriteCityRepository.save(any(FavoriteCity.class))).thenReturn(savedCity);

        // Agora chama passando o isDefault explicitamente
        FavoriteCityResponse response = favoriteCityService.addFavoriteCity("Berlin", true, "testuser");

        assertNotNull(response);
        assertEquals("Berlin", response.getCityName());
        assertTrue(response.getIsDefault());
    }

    @Test
    void getFavoriteCities_returnsList() {
        FavoriteCity city1 = FavoriteCity.builder()
                .id(1L)
                .user(testUser)
                .cityName("Paris")
                .isDefault(false)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        FavoriteCity city2 = FavoriteCity.builder()
                .id(2L)
                .user(testUser)
                .cityName("Madrid")
                .isDefault(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(favoriteCityRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(List.of(city2, city1)); // ordenado decrescente por createdAt

        ForecastResponse forecastParis = ForecastResponse.builder()
                .temperature(10.0)
                .humidity(60)
                .description("rainy")
                .build();

        ForecastResponse forecastMadrid = ForecastResponse.builder()
                .temperature(20.0)
                .humidity(30)
                .description("sunny")
                .build();

        when(forecastService.getCurrentWeather("Paris")).thenReturn(forecastParis);
        when(forecastService.getCurrentWeather("Madrid")).thenReturn(forecastMadrid);

        List<FavoriteCityResponse> favorites = favoriteCityService.getUserFavoriteCities("testuser");

        assertEquals(2, favorites.size());
        assertEquals("Madrid", favorites.get(0).getCityName()); // Madrid vem primeiro por ser mais recente
        assertEquals("Paris", favorites.get(1).getCityName());
    }

    @Test
    void removeFavoriteCity_deletesWhenAuthorized() {
        FavoriteCity city = FavoriteCity.builder().id(1L).user(testUser).cityName("Tokyo").isDefault(false).build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(favoriteCityRepository.findById(1L)).thenReturn(Optional.of(city));
        doNothing().when(favoriteCityRepository).delete(city);

        assertDoesNotThrow(() -> favoriteCityService.removeFavoriteCity(1L, "testuser"));
        verify(favoriteCityRepository, times(1)).delete(city);
    }

    @Test
    void addFavoriteCity_cityAlreadyExists_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(favoriteCityRepository.findByUserAndCityName(testUser, "Berlin"))
                .thenReturn(Optional.of(FavoriteCity.builder().cityName("Berlin").build()));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                favoriteCityService.addFavoriteCity("Berlin", true, "testuser")
        );

        assertEquals("City is already in favorites", exception.getMessage());
    }

    @Test
    void setDefaultCity_successfullySetsNewDefault() {
        FavoriteCity oldDefault = FavoriteCity.builder()
                .id(1L)
                .user(testUser)
                .cityName("Lisbon")
                .isDefault(true)
                .build();

        FavoriteCity newDefault = FavoriteCity.builder()
                .id(2L)
                .user(testUser)
                .cityName("Amsterdam")
                .isDefault(false)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(favoriteCityRepository.findById(2L)).thenReturn(Optional.of(newDefault));
        when(favoriteCityRepository.findByUserAndIsDefaultTrue(testUser)).thenReturn(Optional.of(oldDefault));
        when(favoriteCityRepository.save(any(FavoriteCity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FavoriteCityResponse response = favoriteCityService.setDefaultCity(2L, "testuser");

        assertTrue(response.getIsDefault());
        verify(favoriteCityRepository, times(2)).save(any());
    }

    @Test
    void removeFavoriteCity_unauthorizedUser_throwsException() {
        User otherUser = User.builder().id(2L).username("otheruser").build();
        FavoriteCity city = FavoriteCity.builder().id(1L).user(otherUser).cityName("Rome").isDefault(false).build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(favoriteCityRepository.findById(1L)).thenReturn(Optional.of(city));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                favoriteCityService.removeFavoriteCity(1L, "testuser")
        );

        assertEquals("You don't have permission to remove this favorite city", exception.getMessage());
    }

    @Test
    void addFavoriteCity_resetsOldDefaultIfNewDefaultIsSet() {
        FavoriteCity oldDefault = FavoriteCity.builder()
                .id(1L)
                .user(testUser)
                .cityName("OldCity")
                .isDefault(true)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(favoriteCityRepository.findByUserAndCityName(testUser, "NewCity")).thenReturn(Optional.empty());
        when(favoriteCityRepository.findByUserAndIsDefaultTrue(testUser)).thenReturn(Optional.of(oldDefault));

        // Quando salvar o novo favorito
        when(favoriteCityRepository.save(any(FavoriteCity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FavoriteCityResponse response = favoriteCityService.addFavoriteCity("NewCity", true, "testuser");

        assertNotNull(response);
        assertEquals("NewCity", response.getCityName());
        assertTrue(response.getIsDefault());

        // Verifica se o antigo favorito foi atualizado e salvo com isDefault = false
        assertFalse(oldDefault.getIsDefault());
        verify(favoriteCityRepository, times(2)).save(any()); // Um para oldDefault e outro para o novo favorito
    }

    @Test
    void getDefaultCity_userNotFound_throwsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                favoriteCityService.getDefaultCity("unknown")
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void getDefaultCity_noDefaultCity_returnsNull() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(favoriteCityRepository.findByUserAndIsDefaultTrue(testUser)).thenReturn(Optional.empty());

        FavoriteCityResponse response = favoriteCityService.getDefaultCity("testuser");

        assertNull(response);
    }

    @Test
    void getDefaultCityWeather_returnsForecast() {
        FavoriteCity defaultCity = FavoriteCity.builder()
                .id(1L)
                .user(testUser)
                .cityName("London")
                .isDefault(true)
                .build();

        ForecastResponse forecast = ForecastResponse.builder()
                .temperature(15.0)
                .humidity(50)
                .description("cloudy")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(favoriteCityRepository.findByUserAndIsDefaultTrue(testUser)).thenReturn(Optional.of(defaultCity));
        when(forecastService.getCurrentWeather("London")).thenReturn(forecast);

        ForecastResponse result = favoriteCityService.getDefaultCityWeather("testuser");

        assertEquals(15.0, result.getTemperature());
        assertEquals("cloudy", result.getDescription());
    }

    @Test
    void addFavoriteCity_userNotFound_throwsException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                favoriteCityService.addFavoriteCity("Berlin", true, "nonexistent")
        );

        assertEquals("User not found", exception.getMessage());
    }

}
