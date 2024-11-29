package org.educandoweb.springweatherdata.requests;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WeatherSearchDTO {
    private String city;
    private LocalDateTime searchDate;
    private Double temperature;
    private String description;
}