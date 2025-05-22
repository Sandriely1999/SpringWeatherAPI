package org.educandoweb.springweatherdata.controller;

import org.educandoweb.springweatherdata.entities.WeatherAlert.AlertType;
import org.educandoweb.springweatherdata.responses.AlertResponse;
import org.educandoweb.springweatherdata.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlertControllerTest {

    @InjectMocks
    private AlertController alertController;

    @Mock
    private AlertService alertService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(userDetails.getUsername()).thenReturn("testuser");
    }

    @Test
    void createAlert_returnsAlertResponse() {
        AlertResponse mockResponse = AlertResponse.builder()
                .city("Berlin")
                .alertType(AlertType.HIGH_TEMPERATURE)
                .thresholdValue(25.0)
                .isActive(true)
                .build();

        when(alertService.createAlert("Berlin", AlertType.HIGH_TEMPERATURE, 25.0, "testuser"))
                .thenReturn(mockResponse);

        ResponseEntity<AlertResponse> response = alertController.createAlert("Berlin", AlertType.HIGH_TEMPERATURE, 25.0, userDetails);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Berlin", response.getBody().getCity());
        assertEquals(AlertType.HIGH_TEMPERATURE, response.getBody().getAlertType());
        assertTrue(response.getBody().getIsActive());


        verify(alertService, times(1)).createAlert("Berlin", AlertType.HIGH_TEMPERATURE, 25.0, "testuser");
    }

    @Test
    void getUserAlerts_returnsList() {
        List<AlertResponse> mockList = List.of(
                AlertResponse.builder()
                        .city("Paris")
                        .alertType(AlertType.EXTREME_WEATHER)
                        .thresholdValue(10.0)
                        .isActive(true)
                        .build()
        );
        when(alertService.getUserAlerts("testuser")).thenReturn(mockList);

        ResponseEntity<List<AlertResponse>> response = alertController.getUserAlerts(userDetails);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Paris", response.getBody().get(0).getCity());

        verify(alertService, times(1)).getUserAlerts("testuser");
    }

    @Test
    void toggleAlert_returnsUpdatedAlert() {
        AlertResponse mockResponse = AlertResponse.builder()
                .isActive(false)
                .build();
        when(alertService.toggleAlert(1L, false, "testuser")).thenReturn(mockResponse);

        ResponseEntity<AlertResponse> response = alertController.toggleAlert(1L, false, userDetails);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getIsActive());

        verify(alertService, times(1)).toggleAlert(1L, false, "testuser");
    }

    @Test
    void deleteAlert_callsService() {
        doNothing().when(alertService).deleteAlert(1L, "testuser");

        ResponseEntity<Void> response = alertController.deleteAlert(1L, userDetails);

        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());

        verify(alertService, times(1)).deleteAlert(1L, "testuser");
    }

    @Test
    void checkAlertsForCity_returnsList() {
        List<AlertResponse> mockList = List.of(
                AlertResponse.builder()
                        .city("Rome")
                        .isActive(true)
                        .build()
        );
        when(alertService.checkAlertsForCity("Rome", "testuser")).thenReturn(mockList);

        ResponseEntity<List<AlertResponse>> response = alertController.checkAlertsForCity("Rome", userDetails);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Rome", response.getBody().get(0).getCity());

        verify(alertService, times(1)).checkAlertsForCity("Rome", "testuser");
    }
}
