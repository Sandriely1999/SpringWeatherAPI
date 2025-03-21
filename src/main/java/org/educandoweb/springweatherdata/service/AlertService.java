package org.educandoweb.springweatherdata.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.educandoweb.springweatherdata.entities.User;
import org.educandoweb.springweatherdata.entities.WeatherAlert;
import org.educandoweb.springweatherdata.entities.WeatherAlert.AlertType;
import org.educandoweb.springweatherdata.repositories.UserRepository;
import org.educandoweb.springweatherdata.repositories.WeatherAlertRepository;
import org.educandoweb.springweatherdata.responses.AlertResponse;
import org.educandoweb.springweatherdata.responses.ForecastResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {
    private final WeatherAlertRepository alertRepository;
    private final UserRepository userRepository;
    private final ForecastService forecastService;


    public AlertResponse createAlert(String city, AlertType alertType, Double thresholdValue, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        WeatherAlert alert = WeatherAlert.builder()
                .user(user)
                .city(city)
                .alertType(alertType)
                .thresholdValue(thresholdValue)
                .isActive(true)
                .build();

        WeatherAlert savedAlert = alertRepository.save(alert);

        return AlertResponse.builder()
                .id(savedAlert.getId())
                .city(savedAlert.getCity())
                .alertType(savedAlert.getAlertType())
                .thresholdValue(savedAlert.getThresholdValue())
                .isActive(savedAlert.getIsActive())
                .build();
    }

    public List<AlertResponse> getUserAlerts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return alertRepository.findByUser(user).stream()
                .map(alert -> AlertResponse.builder()
                        .id(alert.getId())
                        .city(alert.getCity())
                        .alertType(alert.getAlertType())
                        .thresholdValue(alert.getThresholdValue())
                        .isActive(alert.getIsActive())
                        .build())
                .collect(Collectors.toList());
    }

    public AlertResponse toggleAlert(Long alertId, boolean active, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        WeatherAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        if (!alert.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to modify this alert");
        }

        alert.setIsActive(active);
        WeatherAlert savedAlert = alertRepository.save(alert);

        return AlertResponse.builder()
                .id(savedAlert.getId())
                .city(savedAlert.getCity())
                .alertType(savedAlert.getAlertType())
                .thresholdValue(savedAlert.getThresholdValue())
                .isActive(savedAlert.getIsActive())
                .build();
    }

    public void deleteAlert(Long alertId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        WeatherAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        if (!alert.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to delete this alert");
        }

        alertRepository.delete(alert);
    }

    public List<AlertResponse> checkAlertsForCity(String city, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<WeatherAlert> activeAlerts = alertRepository.findByUserAndCityAndIsActiveTrue(user, city);
        if (activeAlerts.isEmpty()) {
            return List.of();
        }

        // Buscar dados atuais do clima para a cidade
       ForecastResponse currentWeather = forecastService.getCurrentWeather(city);
        List<AlertResponse> triggeredAlerts = new ArrayList<>();

        for (WeatherAlert alert : activeAlerts) {
            boolean isTriggered = false;

            switch (alert.getAlertType()) {
                case HIGH_TEMPERATURE:
                    isTriggered = currentWeather.getTemperature() > alert.getThresholdValue();
                    break;
                case LOW_TEMPERATURE:
                    isTriggered = currentWeather.getTemperature() < alert.getThresholdValue();
                    break;
                case HIGH_HUMIDITY:
                    isTriggered = currentWeather.getHumidity() > alert.getThresholdValue();
                    break;
                case LOW_HUMIDITY:
                    isTriggered = currentWeather.getHumidity() < alert.getThresholdValue();
                    break;
                case EXTREME_WEATHER:
                    // Para clima extremo, verifique palavras-chave na descrição
                    String description = currentWeather.getDescription().toLowerCase();
                    isTriggered = description.contains("tempestade") ||
                            description.contains("furacão") ||
                            description.contains("nevasca") ||
                            description.contains("tornado");
                    break;
            }

            if (isTriggered) {
                triggeredAlerts.add(AlertResponse.builder()
                        .id(alert.getId())
                        .city(alert.getCity())
                        .alertType(alert.getAlertType())
                        .thresholdValue(alert.getThresholdValue())
                        .isActive(alert.getIsActive())
                        .build());
            }
        }

        return triggeredAlerts;
    }
}