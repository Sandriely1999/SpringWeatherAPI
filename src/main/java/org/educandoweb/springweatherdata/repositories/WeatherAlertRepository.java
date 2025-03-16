package org.educandoweb.springweatherdata.repositories;

import org.educandoweb.springweatherdata.entities.User;
import org.educandoweb.springweatherdata.entities.WeatherAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherAlertRepository extends JpaRepository<WeatherAlert, Long> {
    List<WeatherAlert> findByUser(User user);
    List<WeatherAlert> findByUserAndIsActiveTrue(User user);
    List<WeatherAlert> findByUserAndCityAndIsActiveTrue(User user, String city);
}