package at.co.netconsulting.analyse.repository;

import at.co.netconsulting.analyse.entity.TrackingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrackingSessionRepository extends JpaRepository<TrackingSession, String> {

    @Query(value = """
            SELECT ts.sport_type AS sportType,
                   COUNT(*) AS sessionCount,
                   COALESCE(SUM(g.max_dist), 0) / 1000.0 AS totalDistanceKm,
                   COALESCE(AVG(g.max_dist), 0) / 1000.0 AS avgDistanceKm
            FROM tracking_sessions ts
            LEFT JOIN (
                SELECT session_id, MAX(distance) AS max_dist
                FROM gps_tracking_points
                GROUP BY session_id
            ) g ON ts.session_id = g.session_id
            WHERE ts.sport_type IS NOT NULL
              AND ts.session_id NOT LIKE '%\\_reset\\_%'
              AND (:userId IS NULL OR ts.user_id = :userId)
            GROUP BY ts.sport_type
            ORDER BY sessionCount DESC
            """, nativeQuery = true)
    List<Object[]> findSportTypeStats(@Param("userId") Integer userId);

    @Query(value = """
            SELECT u.firstname AS userName,
                   COUNT(ts.session_id) AS sessionCount,
                   COALESCE(SUM(g.max_dist), 0) / 1000.0 AS totalDistanceKm,
                   COALESCE(
                       SUM(CASE WHEN g.duration_seconds >= 60 THEN g.max_dist ELSE 0 END)
                       / NULLIF(SUM(CASE WHEN g.duration_seconds >= 60 THEN g.duration_seconds ELSE 0 END), 0)
                       * 3.6,
                       0
                   ) AS avgSpeedKmh,
                   COALESCE(SUM(g.max_elev), 0) AS totalElevationGainM,
                   COALESCE(AVG(g.avg_hr), 0) AS avgHeartRate
            FROM users u
            JOIN tracking_sessions ts ON u.user_id = ts.user_id
            LEFT JOIN (
                SELECT p.session_id,
                       p.max_dist,
                       CASE
                           WHEN p.received_duration_seconds >= 60
                            AND p.max_dist / p.received_duration_seconds * 3.6
                                BETWEEN 0.5 AND 200
                           THEN p.received_duration_seconds
                           WHEN p.created_duration_seconds >= 60
                            AND p.max_dist / p.created_duration_seconds * 3.6
                                BETWEEN 0.5 AND 200
                           THEN p.created_duration_seconds
                           WHEN p.max_dist > 0
                            AND p.final_average_speed BETWEEN 0.5 AND 200
                           THEN p.max_dist / p.final_average_speed * 3.6
                           ELSE NULL
                       END AS duration_seconds,
                       p.max_elev,
                       p.avg_hr
                FROM (
                    SELECT session_id,
                           MAX(distance) AS max_dist,
                           EXTRACT(EPOCH FROM MAX(received_at) - MIN(received_at))
                               AS received_duration_seconds,
                           EXTRACT(EPOCH FROM MAX(created_at) - MIN(created_at))
                               AS created_duration_seconds,
                           (ARRAY_AGG(
                               average_speed
                               ORDER BY distance DESC NULLS LAST, id DESC
                           ) FILTER (WHERE average_speed > 0))[1] AS final_average_speed,
                           MAX(cumulative_elevation_gain) AS max_elev,
                           AVG(heart_rate) FILTER (WHERE heart_rate > 0) AS avg_hr
                    FROM gps_tracking_points
                    GROUP BY session_id
                ) p
            ) g ON ts.session_id = g.session_id
            WHERE ts.session_id NOT LIKE '%\\_reset\\_%'
              AND (:userId IS NULL OR ts.user_id = :userId)
            GROUP BY u.user_id, u.firstname
            ORDER BY totalDistanceKm DESC
            """, nativeQuery = true)
    List<Object[]> findUserStats(@Param("userId") Integer userId);

    @Query(value = """
            SELECT TO_CHAR(ts.start_date_time, 'YYYY-MM') AS yearMonth,
                   COUNT(*) AS sessionCount,
                   COALESCE(SUM(g.max_dist), 0) / 1000.0 AS totalDistanceKm
            FROM tracking_sessions ts
            LEFT JOIN (
                SELECT session_id, MAX(distance) AS max_dist
                FROM gps_tracking_points
                GROUP BY session_id
            ) g ON ts.session_id = g.session_id
            WHERE ts.start_date_time IS NOT NULL
              AND ts.session_id NOT LIKE '%\\_reset\\_%'
              AND (:userId IS NULL OR ts.user_id = :userId)
            GROUP BY TO_CHAR(ts.start_date_time, 'YYYY-MM')
            ORDER BY yearMonth DESC
            """, nativeQuery = true)
    List<Object[]> findMonthlyStats(@Param("userId") Integer userId);

    @Query(value = """
            SELECT ts.session_id,
                   ts.event_name,
                   ts.sport_type,
                   ts.start_date_time,
                   ts.start_city,
                   ts.start_country,
                   ts.comment,
                   u.firstname,
                   COALESCE(g.max_dist, 0) / 1000.0 AS distanceKm,
                   COALESCE(g.avg_speed, 0) AS avgSpeedKmh,
                   COALESCE(g.top_speed, 0) AS maxSpeedKmh,
                   COALESCE(g.elev_gain, 0) AS elevationGainM,
                   g.avg_hr AS avgHeartRate,
                   g.avg_temp AS avgTemperature,
                   g.duration_seconds AS durationSeconds
            FROM tracking_sessions ts
            JOIN users u ON ts.user_id = u.user_id
            LEFT JOIN (
                SELECT p.session_id,
                       p.max_dist,
                       CASE
                           WHEN p.max_dist > 0 AND p.duration_seconds >= 1
                           THEN p.max_dist / p.duration_seconds * 3.6
                           ELSE 0
                       END AS avg_speed,
                       p.top_speed,
                       p.elev_gain,
                       p.avg_hr,
                       p.avg_temp,
                       p.duration_seconds
                FROM (
                    SELECT session_id,
                           MAX(distance) AS max_dist,
                           MAX(max_speed) AS top_speed,
                           MAX(cumulative_elevation_gain) AS elev_gain,
                           AVG(heart_rate) FILTER (WHERE heart_rate > 0) AS avg_hr,
                           AVG(temperature) FILTER (WHERE temperature IS NOT NULL) AS avg_temp,
                           CASE
                               WHEN EXTRACT(EPOCH FROM MAX(received_at) - MIN(received_at)) >= 60
                               THEN EXTRACT(EPOCH FROM MAX(received_at) - MIN(received_at))
                               ELSE COALESCE(
                                   EXTRACT(EPOCH FROM MAX(created_at) - MIN(created_at)),
                                   EXTRACT(EPOCH FROM MAX(received_at) - MIN(received_at))
                               )
                           END AS duration_seconds
                    FROM gps_tracking_points
                    GROUP BY session_id
                ) p
            ) g ON ts.session_id = g.session_id
            WHERE ts.start_date_time IS NOT NULL
              AND ts.session_id NOT LIKE '%\\_reset\\_%'
              AND (:userId IS NULL OR ts.user_id = :userId)
              AND (
                :search IS NULL OR :search = '' OR
                CONCAT_WS(' ',
                    u.firstname,
                    ts.event_name,
                    ts.sport_type,
                    ts.start_city,
                    ts.start_country,
                    TO_CHAR(ts.start_date_time, 'YYYY-MM-DD HH24:MI'),
                    TO_CHAR(ts.start_date_time, 'DD Mon YYYY HH24:MI'),
                    ROUND((COALESCE(g.max_dist, 0) / 1000.0)::numeric, 2)::text,
                    ROUND(COALESCE(g.avg_speed, 0)::numeric, 1)::text || ' km/h',
                    ROUND(COALESCE(g.top_speed, 0)::numeric, 1)::text || ' km/h',
                    ROUND(COALESCE(g.elev_gain, 0)::numeric, 0)::text,
                    CASE WHEN g.avg_hr IS NOT NULL THEN ROUND(g.avg_hr::numeric, 0)::text END,
                    CASE WHEN g.avg_temp IS NOT NULL THEN ROUND(g.avg_temp::numeric, 1)::text || '°C' END,
                    CASE
                        WHEN g.duration_seconds IS NULL OR g.duration_seconds <= 0 THEN NULL
                        WHEN g.duration_seconds::int >= 3600 THEN
                            (g.duration_seconds::int / 3600) || 'h '
                            || LPAD(((g.duration_seconds::int % 3600) / 60)::text, 2, '0') || 'm '
                            || LPAD((g.duration_seconds::int % 60)::text, 2, '0') || 's'
                        ELSE
                            (g.duration_seconds::int / 60) || 'm '
                            || LPAD((g.duration_seconds::int % 60)::text, 2, '0') || 's'
                    END,
                    CASE
                        WHEN g.duration_seconds IS NULL OR g.duration_seconds <= 0
                          OR g.max_dist IS NULL OR g.max_dist <= 0 THEN NULL
                        ELSE
                            FLOOR((g.duration_seconds / 60.0) / (g.max_dist / 1000.0))::int::text
                            || ':'
                            || LPAD(FLOOR(((g.duration_seconds / 60.0) / (g.max_dist / 1000.0)
                                    - FLOOR((g.duration_seconds / 60.0) / (g.max_dist / 1000.0))) * 60)::int::text, 2, '0')
                            || ' min/km'
                    END
                ) ILIKE CONCAT('%', :search, '%')
              )
            ORDER BY ts.start_date_time DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Object[]> findRecentSessionsWithStats(@Param("userId") Integer userId,
                                               @Param("search") String search,
                                               @Param("limit") int limit,
                                               @Param("offset") int offset);

    @Query(value = """
            SELECT COUNT(*)
            FROM tracking_sessions ts
            WHERE ts.start_date_time IS NOT NULL
              AND ts.session_id NOT LIKE '%\\_reset\\_%'
              AND (:userId IS NULL OR ts.user_id = :userId)
            """, nativeQuery = true)
    long countSessions(@Param("userId") Integer userId);

    @Query(value = """
            SELECT COUNT(*)
            FROM tracking_sessions ts
            JOIN users u ON ts.user_id = u.user_id
            LEFT JOIN (
                SELECT p.session_id,
                       p.max_dist,
                       CASE
                           WHEN p.max_dist > 0 AND p.duration_seconds >= 1
                           THEN p.max_dist / p.duration_seconds * 3.6
                           ELSE 0
                       END AS avg_speed,
                       p.top_speed,
                       p.elev_gain,
                       p.avg_hr,
                       p.avg_temp,
                       p.duration_seconds
                FROM (
                    SELECT session_id,
                           MAX(distance) AS max_dist,
                           MAX(max_speed) AS top_speed,
                           MAX(cumulative_elevation_gain) AS elev_gain,
                           AVG(heart_rate) FILTER (WHERE heart_rate > 0) AS avg_hr,
                           AVG(temperature) FILTER (WHERE temperature IS NOT NULL) AS avg_temp,
                           CASE
                               WHEN EXTRACT(EPOCH FROM MAX(received_at) - MIN(received_at)) >= 60
                               THEN EXTRACT(EPOCH FROM MAX(received_at) - MIN(received_at))
                               ELSE COALESCE(
                                   EXTRACT(EPOCH FROM MAX(created_at) - MIN(created_at)),
                                   EXTRACT(EPOCH FROM MAX(received_at) - MIN(received_at))
                               )
                           END AS duration_seconds
                    FROM gps_tracking_points
                    GROUP BY session_id
                ) p
            ) g ON ts.session_id = g.session_id
            WHERE ts.start_date_time IS NOT NULL
              AND ts.session_id NOT LIKE '%\\_reset\\_%'
              AND (:userId IS NULL OR ts.user_id = :userId)
              AND CONCAT_WS(' ',
                    u.firstname,
                    ts.event_name,
                    ts.sport_type,
                    ts.start_city,
                    ts.start_country,
                    TO_CHAR(ts.start_date_time, 'YYYY-MM-DD HH24:MI'),
                    TO_CHAR(ts.start_date_time, 'DD Mon YYYY HH24:MI'),
                    ROUND((COALESCE(g.max_dist, 0) / 1000.0)::numeric, 2)::text,
                    ROUND(COALESCE(g.avg_speed, 0)::numeric, 1)::text || ' km/h',
                    ROUND(COALESCE(g.top_speed, 0)::numeric, 1)::text || ' km/h',
                    ROUND(COALESCE(g.elev_gain, 0)::numeric, 0)::text,
                    CASE WHEN g.avg_hr IS NOT NULL THEN ROUND(g.avg_hr::numeric, 0)::text END,
                    CASE WHEN g.avg_temp IS NOT NULL THEN ROUND(g.avg_temp::numeric, 1)::text || '°C' END,
                    CASE
                        WHEN g.duration_seconds IS NULL OR g.duration_seconds <= 0 THEN NULL
                        WHEN g.duration_seconds::int >= 3600 THEN
                            (g.duration_seconds::int / 3600) || 'h '
                            || LPAD(((g.duration_seconds::int % 3600) / 60)::text, 2, '0') || 'm '
                            || LPAD((g.duration_seconds::int % 60)::text, 2, '0') || 's'
                        ELSE
                            (g.duration_seconds::int / 60) || 'm '
                            || LPAD((g.duration_seconds::int % 60)::text, 2, '0') || 's'
                    END,
                    CASE
                        WHEN g.duration_seconds IS NULL OR g.duration_seconds <= 0
                          OR g.max_dist IS NULL OR g.max_dist <= 0 THEN NULL
                        ELSE
                            FLOOR((g.duration_seconds / 60.0) / (g.max_dist / 1000.0))::int::text
                            || ':'
                            || LPAD(FLOOR(((g.duration_seconds / 60.0) / (g.max_dist / 1000.0)
                                    - FLOOR((g.duration_seconds / 60.0) / (g.max_dist / 1000.0))) * 60)::int::text, 2, '0')
                            || ' min/km'
                    END
                ) ILIKE CONCAT('%', :search, '%')
            """, nativeQuery = true)
    long countSessionsFiltered(@Param("userId") Integer userId,
                               @Param("search") String search);

    @Query(value = """
            SELECT ts.session_id,
                   ts.event_name,
                   ts.sport_type,
                   ts.start_date_time,
                   ts.start_city,
                   ts.start_country,
                   ts.start_address,
                   ts.end_city,
                   ts.end_country,
                   ts.end_address,
                   ts.comment,
                   ts.clothing,
                   u.firstname,
                   COALESCE(g.max_dist, 0) / 1000.0 AS distanceKm,
                   COALESCE(g.avg_speed, 0) AS avgSpeedKmh,
                   COALESCE(g.top_speed, 0) AS maxSpeedKmh,
                   COALESCE(g.elev_gain, 0) AS elevationGainM,
                   g.avg_hr AS avgHeartRate,
                   g.min_hr AS minHeartRate,
                   g.max_hr AS maxHeartRate,
                   g.avg_temp AS avgTemperature,
                   g.duration_seconds AS durationSeconds
            FROM tracking_sessions ts
            JOIN users u ON ts.user_id = u.user_id
            LEFT JOIN (
                SELECT p.session_id,
                       p.max_dist,
                       CASE
                           WHEN p.max_dist > 0 AND p.duration_seconds >= 1
                           THEN p.max_dist / p.duration_seconds * 3.6
                           ELSE 0
                       END AS avg_speed,
                       p.top_speed,
                       p.elev_gain,
                       p.avg_hr,
                       p.min_hr,
                       p.max_hr,
                       p.avg_temp,
                       p.duration_seconds
                FROM (
                    SELECT session_id,
                           MAX(distance) AS max_dist,
                           MAX(max_speed) AS top_speed,
                           MAX(cumulative_elevation_gain) AS elev_gain,
                           AVG(heart_rate) FILTER (WHERE heart_rate > 0) AS avg_hr,
                           MIN(heart_rate) FILTER (WHERE heart_rate > 0) AS min_hr,
                           MAX(heart_rate) FILTER (WHERE heart_rate > 0) AS max_hr,
                           AVG(temperature) FILTER (WHERE temperature IS NOT NULL) AS avg_temp,
                           CASE
                               WHEN EXTRACT(EPOCH FROM MAX(received_at) - MIN(received_at)) >= 60
                               THEN EXTRACT(EPOCH FROM MAX(received_at) - MIN(received_at))
                               ELSE COALESCE(
                                   EXTRACT(EPOCH FROM MAX(created_at) - MIN(created_at)),
                                   EXTRACT(EPOCH FROM MAX(received_at) - MIN(received_at))
                               )
                           END AS duration_seconds
                    FROM gps_tracking_points
                    WHERE session_id = :sessionId
                    GROUP BY session_id
                ) p
            ) g ON ts.session_id = g.session_id
            WHERE ts.session_id = :sessionId
            """, nativeQuery = true)
    List<Object[]> findSessionDetail(@Param("sessionId") String sessionId);

    @Query(value = """
            WITH timed_points AS (
                SELECT gps_tracking_points.*,
                       CASE
                           WHEN EXTRACT(EPOCH FROM
                               MAX(received_at) OVER () - MIN(received_at) OVER ()
                           ) >= 60
                           THEN COALESCE(received_at, created_at)
                           ELSE COALESCE(created_at, received_at)
                       END AS event_at
                FROM gps_tracking_points
                WHERE session_id = :sessionId
                  AND latitude IS NOT NULL
                  AND longitude IS NOT NULL
            )
            SELECT latitude,
                   longitude,
                   altitude,
                   heart_rate,
                   distance,
                   current_speed,
                   EXTRACT(EPOCH FROM
                       event_at - MIN(event_at) OVER ()
                   ) AS seconds_from_start,
                   temperature,
                   wind_speed,
                   wind_direction,
                   humidity,
                   weather_code,
                   pressure,
                   sea_level_pressure,
                   altitude_from_pressure,
                   slope,
                   horizontal_accuracy,
                   number_of_satellites
            FROM timed_points
            ORDER BY event_at ASC
            """, nativeQuery = true)
    List<Object[]> findSessionTrackPoints(@Param("sessionId") String sessionId);

    @Query(value = """
            SELECT COUNT(*) AS totalSessions,
                   COUNT(DISTINCT ts.user_id) AS activeUsers,
                   COALESCE(SUM(g.max_dist), 0) / 1000.0 AS totalDistanceKm,
                   COALESCE(SUM(g.point_count), 0) AS totalGpsPoints
            FROM tracking_sessions ts
            LEFT JOIN (
                SELECT session_id,
                       MAX(distance) AS max_dist,
                       COUNT(*) AS point_count
                FROM gps_tracking_points
                GROUP BY session_id
            ) g ON ts.session_id = g.session_id
            WHERE ts.session_id NOT LIKE '%\\_reset\\_%'
              AND (:userId IS NULL OR ts.user_id = :userId)
            """, nativeQuery = true)
    List<Object[]> findOverallStats(@Param("userId") Integer userId);

    @Query(value = """
            SELECT 'Longest Distance' AS record_type,
                   ts.event_name, u.firstname, ts.sport_type,
                   MAX(g.distance) / 1000.0 AS value,
                   'km' AS unit,
                   ts.start_date_time
            FROM gps_tracking_points g
            JOIN tracking_sessions ts ON g.session_id = ts.session_id
            JOIN users u ON ts.user_id = u.user_id
            WHERE ts.session_id NOT LIKE '%\\_reset\\_%'
              AND (:userId IS NULL OR ts.user_id = :userId)
            GROUP BY ts.session_id, ts.event_name, u.firstname, ts.sport_type, ts.start_date_time
            ORDER BY MAX(g.distance) DESC
            LIMIT 1
            """, nativeQuery = true)
    List<Object[]> findLongestDistanceRecord(@Param("userId") Integer userId);

    @Query(value = """
            SELECT 'Highest Elevation Gain' AS record_type,
                   ts.event_name, u.firstname, ts.sport_type,
                   MAX(g.cumulative_elevation_gain) AS value,
                   'm' AS unit,
                   ts.start_date_time
            FROM gps_tracking_points g
            JOIN tracking_sessions ts ON g.session_id = ts.session_id
            JOIN users u ON ts.user_id = u.user_id
            WHERE ts.session_id NOT LIKE '%\\_reset\\_%'
              AND (:userId IS NULL OR ts.user_id = :userId)
            GROUP BY ts.session_id, ts.event_name, u.firstname, ts.sport_type, ts.start_date_time
            ORDER BY MAX(g.cumulative_elevation_gain) DESC
            LIMIT 1
            """, nativeQuery = true)
    List<Object[]> findHighestElevationRecord(@Param("userId") Integer userId);

    @Query(value = """
            SELECT 'Fastest Average Speed' AS record_type,
                   ts.event_name, u.firstname, ts.sport_type,
                   g.max_dist / g.duration_seconds * 3.6 AS value,
                   'km/h' AS unit,
                   ts.start_date_time
            FROM tracking_sessions ts
            JOIN users u ON ts.user_id = u.user_id
            JOIN (
                SELECT session_id,
                       MAX(distance) AS max_dist,
                       CASE
                           WHEN EXTRACT(EPOCH FROM MAX(received_at) - MIN(received_at)) >= 60
                           THEN EXTRACT(EPOCH FROM MAX(received_at) - MIN(received_at))
                           ELSE COALESCE(
                               EXTRACT(EPOCH FROM MAX(created_at) - MIN(created_at)),
                               EXTRACT(EPOCH FROM MAX(received_at) - MIN(received_at))
                           )
                       END AS duration_seconds
                FROM gps_tracking_points
                GROUP BY session_id
            ) g ON ts.session_id = g.session_id
            WHERE ts.sport_type IN ('Running', 'Marathon', 'Ultramarathon', 'Road Running', 'Training')
              AND ts.session_id NOT LIKE '%\\_reset\\_%'
              AND (:userId IS NULL OR ts.user_id = :userId)
              AND g.max_dist > 1000
              AND g.duration_seconds >= 60
            ORDER BY g.max_dist / g.duration_seconds DESC
            LIMIT 1
            """, nativeQuery = true)
    List<Object[]> findFastestRunRecord(@Param("userId") Integer userId);

    @Query(value = """
            SELECT ts.session_id,
                   ts.event_name,
                   u.firstname,
                   ts.sport_type,
                   ts.start_date_time,
                   ts.start_city,
                   ts.start_country,
                   g.max_dist / 1000.0 AS distance_km,
                   g.duration_seconds,
                   (g.duration_seconds / 60.0) / (g.max_dist / 1000.0) AS pace_min_per_km
            FROM tracking_sessions ts
            JOIN users u ON ts.user_id = u.user_id
            JOIN (
                SELECT session_id,
                       MAX(distance) AS max_dist,
                       CASE
                           WHEN EXTRACT(EPOCH FROM MAX(received_at) - MIN(received_at)) >= 60
                           THEN EXTRACT(EPOCH FROM MAX(received_at) - MIN(received_at))
                           ELSE COALESCE(
                               EXTRACT(EPOCH FROM MAX(created_at) - MIN(created_at)),
                               EXTRACT(EPOCH FROM MAX(received_at) - MIN(received_at))
                           )
                       END AS duration_seconds
                FROM gps_tracking_points
                GROUP BY session_id
            ) g ON ts.session_id = g.session_id
            WHERE g.max_dist > 1000
              AND g.duration_seconds > 60
              AND ts.sport_type IN ('Running', 'Marathon', 'Ultramarathon', 'Road Running', 'Training')
              AND ts.session_id NOT LIKE '%\\_reset\\_%'
              AND (:userId IS NULL OR ts.user_id = :userId)
            ORDER BY pace_min_per_km ASC
            LIMIT 20
            """, nativeQuery = true)
    List<Object[]> findFastestSessionPaces(@Param("userId") Integer userId);
}
