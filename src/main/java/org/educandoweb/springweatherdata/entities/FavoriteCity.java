package org.educandoweb.springweatherdata.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorite_cities",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "city_name"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteCity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "city_name", nullable = false)
    private String cityName;

    private Boolean isDefault;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}