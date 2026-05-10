package at.co.netconsulting.analyse.repository;

import at.co.netconsulting.analyse.entity.PlannedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PlannedEventRepository extends JpaRepository<PlannedEvent, Integer> {

    List<PlannedEvent> findByPlannedEventDateGreaterThanEqualOrderByPlannedEventDateAsc(LocalDate date);

    List<PlannedEvent> findAllByOrderByPlannedEventDateDesc();
}
