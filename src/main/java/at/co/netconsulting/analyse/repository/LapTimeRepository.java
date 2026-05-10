package at.co.netconsulting.analyse.repository;

import at.co.netconsulting.analyse.entity.LapTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LapTimeRepository extends JpaRepository<LapTime, Integer> {

    @Query(value = """
            SELECT lt.session_id,
                   ts.event_name,
                   u.firstname,
                   lt.lap_number,
                   lt.duration,
                   lt.distance,
                   ts.sport_type
            FROM lap_times lt
            JOIN tracking_sessions ts ON lt.session_id = ts.session_id
            JOIN users u ON lt.user_id = u.user_id
            WHERE lt.duration > 60000
            ORDER BY lt.duration ASC
            LIMIT 20
            """, nativeQuery = true)
    List<Object[]> findFastestLaps();

    List<LapTime> findBySessionIdOrderByLapNumberAsc(String sessionId);
}
