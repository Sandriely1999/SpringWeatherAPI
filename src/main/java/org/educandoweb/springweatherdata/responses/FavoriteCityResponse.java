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
public class FavoriteCityResponse {
    private Long id;
    private String cityName;
    private Boolean isDefault;
    private LocalDateTime createdAt;
}