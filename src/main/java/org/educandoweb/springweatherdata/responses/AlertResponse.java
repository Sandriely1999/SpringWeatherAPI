package org.educandoweb.springweatherdata.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.educandoweb.springweatherdata.entities.WeatherAlert.AlertType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {
    private Long id;
    private String city;
    private AlertType alertType;
    private Double thresholdValue;
    private Boolean isActive;
}