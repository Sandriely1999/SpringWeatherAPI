package org.educandoweb.springweatherdata.repositories;

import org.educandoweb.springweatherdata.entities.FavoriteCity;
import org.educandoweb.springweatherdata.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteCityRepository extends JpaRepository<FavoriteCity, Long> {
    List<FavoriteCity> findByUserOrderByCreatedAtDesc(User user);
    Optional<FavoriteCity> findByUserAndCityName(User user, String cityName);
    Optional<FavoriteCity> findByUserAndIsDefaultTrue(User user);
}