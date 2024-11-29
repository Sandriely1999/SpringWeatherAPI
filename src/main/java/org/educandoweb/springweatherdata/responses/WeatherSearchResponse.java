package org.educandoweb.springweatherdata.responses;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class WeatherSearchResponse {
    private String city;
    private Double temperature;
    private Integer humidity;
    private String description;
    private LocalDateTime searchDate;
}