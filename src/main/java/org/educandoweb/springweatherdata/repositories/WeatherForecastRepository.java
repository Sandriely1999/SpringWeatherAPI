package org.educandoweb.springweatherdata.repositories;

import org.educandoweb.springweatherdata.entities.User;
import org.educandoweb.springweatherdata.entities.WeatherForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherForecastRepository extends JpaRepository<WeatherForecast, Long> {
    List<WeatherForecast> findByUserOrderByForecastDateAsc(User user);
    List<WeatherForecast> findByUserAndCityOrderByForecastDateAsc(User user, String city);
}