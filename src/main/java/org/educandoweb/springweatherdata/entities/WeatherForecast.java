package org.educandoweb.springweatherdata.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_forecasts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherForecast {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String city;

    private Double temperature;

    private Integer humidity;

    private String description;

    @Column(nullable = false)
    private LocalDateTime forecastDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}