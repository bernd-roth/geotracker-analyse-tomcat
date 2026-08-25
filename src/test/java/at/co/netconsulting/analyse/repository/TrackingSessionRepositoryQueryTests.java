package at.co.netconsulting.analyse.repository;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackingSessionRepositoryQueryTests {

    @Test
    void gpsTimeQueriesUseReceivedAtWithCreatedAtFallback() {
        String queries = Arrays.stream(TrackingSessionRepository.class.getDeclaredMethods())
                .map(method -> method.getAnnotation(Query.class))
                .filter(query -> query != null && query.nativeQuery())
                .map(Query::value)
                .filter(query -> query.contains("gps_tracking_points"))
                .reduce("", (left, right) -> left + "\n" + right);

        assertTrue(queries.contains("COALESCE(received_at, created_at)"));
        assertTrue(queries.contains("COALESCE(g.received_at, g.created_at)"));
        assertFalse(queries.contains("MAX(created_at)"));
        assertFalse(queries.contains("MIN(created_at)"));
        assertFalse(queries.contains("MAX(g.created_at)"));
        assertFalse(queries.contains("MIN(g.created_at)"));
    }

    @Test
    void sessionTrackPointsUseEventTimeForElapsedSecondsAndOrdering() throws Exception {
        Method method = TrackingSessionRepository.class.getDeclaredMethod(
                "findSessionTrackPoints", String.class);
        String query = method.getAnnotation(Query.class).value();

        assertTrue(query.contains("COALESCE(received_at, created_at)"));
        assertTrue(query.contains("ORDER BY COALESCE(received_at, created_at) ASC"));
    }
}
