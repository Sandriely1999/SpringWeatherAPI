package repositories;

import model.entities.User;
import model.entities.WeatherSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherSearchRepository extends JpaRepository<WeatherSearch, Long> {
    List<WeatherSearch> findByUserOrderBySearchDateDesc(User user);
}

