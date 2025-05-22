package org.educandoweb.springweatherdata.service;

import org.educandoweb.springweatherdata.entities.User;
import org.educandoweb.springweatherdata.entities.WeatherAlert;
import org.educandoweb.springweatherdata.entities.WeatherAlert.AlertType;
import org.educandoweb.springweatherdata.repositories.UserRepository;
import org.educandoweb.springweatherdata.repositories.WeatherAlertRepository;
import org.educandoweb.springweatherdata.responses.AlertResponse;
import org.educandoweb.springweatherdata.responses.ForecastResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlertServiceTest {

    @InjectMocks
    private AlertService alertService;

    @Mock
    private WeatherAlertRepository alertRepository;

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
    void createAlert_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        WeatherAlert alertToSave = WeatherAlert.builder()
                .user(testUser)
                .city("London")
                .alertType(AlertType.HIGH_TEMPERATURE)
                .thresholdValue(30.0)
                .isActive(true)
                .build();

        WeatherAlert savedAlert = WeatherAlert.builder()
                .id(100L)
                .user(testUser)
                .city("London")
                .alertType(AlertType.HIGH_TEMPERATURE)
                .thresholdValue(30.0)
                .isActive(true)
                .build();

        when(alertRepository.save(any(WeatherAlert.class))).thenReturn(savedAlert);

        AlertResponse response = alertService.createAlert("London", AlertType.HIGH_TEMPERATURE, 30.0, "testuser");

        assertNotNull(response);
        assertEquals(savedAlert.getId(), response.getId());
        assertEquals("London", response.getCity());
        assertEquals(AlertType.HIGH_TEMPERATURE, response.getAlertType());
        assertTrue(response.getIsActive());
    }

    @Test
    void getUserAlerts_returnsList() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        WeatherAlert alert1 = WeatherAlert.builder().id(1L).city("Paris").alertType(AlertType.LOW_TEMPERATURE).thresholdValue(5.0).isActive(true).user(testUser).build();
        WeatherAlert alert2 = WeatherAlert.builder().id(2L).city("Berlin").alertType(AlertType.HIGH_HUMIDITY).thresholdValue(70.0).isActive(false).user(testUser).build();

        when(alertRepository.findByUser(testUser)).thenReturn(List.of(alert1, alert2));

        List<AlertResponse> responses = alertService.getUserAlerts("testuser");

        assertEquals(2, responses.size());
        assertEquals("Paris", responses.get(0).getCity());
        assertEquals("Berlin", responses.get(1).getCity());
    }

    @Test
    void toggleAlert_updatesStatus() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        WeatherAlert alert = WeatherAlert.builder().id(10L).user(testUser).city("Rome").alertType(AlertType.HIGH_TEMPERATURE).thresholdValue(35.0).isActive(true).build();

        when(alertRepository.findById(10L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(WeatherAlert.class))).thenAnswer(i -> i.getArgument(0));

        AlertResponse response = alertService.toggleAlert(10L, false, "testuser");

        assertFalse(response.getIsActive());
    }

    @Test
    void deleteAlert_deletesWhenAuthorized() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        WeatherAlert alert = WeatherAlert.builder().id(5L).user(testUser).build();

        when(alertRepository.findById(5L)).thenReturn(Optional.of(alert));
        doNothing().when(alertRepository).delete(alert);

        assertDoesNotThrow(() -> alertService.deleteAlert(5L, "testuser"));
        verify(alertRepository, times(1)).delete(alert);
    }

    @Test
    void checkAlertsForCity_triggersAllAlertTypes() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Criando 5 alertas, um de cada tipo
        WeatherAlert alertHighTemp = WeatherAlert.builder()
                .id(1L).user(testUser).city("MegaCity").alertType(AlertType.HIGH_TEMPERATURE)
                .thresholdValue(25.0).isActive(true).build();

        WeatherAlert alertLowTemp = WeatherAlert.builder()
                .id(2L).user(testUser).city("MegaCity").alertType(AlertType.LOW_TEMPERATURE)
                .thresholdValue(35.0).isActive(true).build(); // mais alto para permitir que 30.0 dispare também

        WeatherAlert alertHighHum = WeatherAlert.builder()
                .id(3L).user(testUser).city("MegaCity").alertType(AlertType.HIGH_HUMIDITY)
                .thresholdValue(70.0).isActive(true).build();

        WeatherAlert alertLowHum = WeatherAlert.builder()
                .id(4L).user(testUser).city("MegaCity").alertType(AlertType.LOW_HUMIDITY)
                .thresholdValue(90.0).isActive(true).build(); // maior que a umidade de teste

        WeatherAlert alertExtreme = WeatherAlert.builder()
                .id(5L).user(testUser).city("MegaCity").alertType(AlertType.EXTREME_WEATHER)
                .thresholdValue(0.0).isActive(true).build();

        List<WeatherAlert> alerts = List.of(alertHighTemp, alertLowTemp, alertHighHum, alertLowHum, alertExtreme);
        when(alertRepository.findByUserAndCityAndIsActiveTrue(testUser, "MegaCity")).thenReturn(alerts);

        // Condições que disparam todos os alertas
        ForecastResponse forecast = ForecastResponse.builder()
                .temperature(30.0) // > 25.0 (HIGH) e < 35.0 (LOW)
                .humidity(80)      // > 70.0 (HIGH), < 90.0 (LOW)
                .description("Tempestade severa e furacão") // EXTREME
                .build();

        when(forecastService.getCurrentWeather("MegaCity")).thenReturn(forecast);

        List<AlertResponse> triggered = alertService.checkAlertsForCity("MegaCity", "testuser");

        assertEquals(5, triggered.size());

        // Validando que todos os IDs esperados foram disparados
        List<Long> triggeredIds = triggered.stream().map(AlertResponse::getId).toList();
        assertTrue(triggeredIds.containsAll(List.of(1L, 2L, 3L, 4L, 5L)));
    }


}
