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
public class ForecastResponse {
    private String city;
    private Double temperature;
    private Integer humidity;
    private String description;
    private LocalDateTime forecastDate;
}