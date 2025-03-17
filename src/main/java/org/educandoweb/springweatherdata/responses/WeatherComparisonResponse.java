package org.educandoweb.springweatherdata.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherComparisonResponse {
    private String cityA;
    private String cityB;
    private Double temperatureA;
    private Double temperatureB;
    private Double temperatureDifference;
    private Integer humidityA;
    private Integer humidityB;
    private Integer humidityDifference;
    private String descriptionA;
    private String descriptionB;
    private LocalDateTime comparisonDate;
}