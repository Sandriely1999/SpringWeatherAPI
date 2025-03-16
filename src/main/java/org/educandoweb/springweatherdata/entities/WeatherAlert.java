package org.educandoweb.springweatherdata.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "weather_alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String city;

    private AlertType alertType;

    private Double thresholdValue;

    private Boolean isActive;

    public enum AlertType {
        HIGH_TEMPERATURE,
        LOW_TEMPERATURE,
        HIGH_HUMIDITY,
        LOW_HUMIDITY,
        EXTREME_WEATHER
    }
}