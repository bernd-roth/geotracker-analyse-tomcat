package at.co.netconsulting.analyse.repository;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackingSessionRepositoryQueryTests {

    @Test
    void durationQueriesPreferUsableReceivedSpanAndFallBackToCreatedSpan() throws Exception {
        List<String> methodNames = List.of(
                "findRecentSessionsWithStats",
                "countSessionsFiltered",
                "findSessionDetail",
                "findFastestRunRecord",
                "findFastestSessionPaces"
        );

        for (String methodName : methodNames) {
            String query = queryFor(methodName);

            assertTrue(query.contains(
                    "WHEN EXTRACT(EPOCH FROM MAX(received_at) - MIN(received_at)) >= 60"),
                    methodName + " must reject a compressed received_at timeline");
            assertTrue(query.contains(
                    "THEN EXTRACT(EPOCH FROM MAX(received_at) - MIN(received_at))"),
                    methodName + " must prefer a usable received_at timeline");
            assertTrue(query.contains(
                    "EXTRACT(EPOCH FROM MAX(created_at) - MIN(created_at))"),
                    methodName + " must fall back to the created_at timeline");
            assertFalse(query.contains("MAX(COALESCE(received_at, created_at))"),
                    methodName + " must select the timeline per session, not per point");
        }
    }

    @Test
    void userStatsInferDurationFromFinalRunningAverageWhenBothTimelinesAreCompressed()
            throws Exception {
        String query = queryFor("findUserStats");

        assertTrue(query.contains("WHEN p.received_duration_seconds >= 60"));
        assertTrue(query.contains("p.max_dist / p.received_duration_seconds * 3.6"));
        assertTrue(query.contains("WHEN p.created_duration_seconds >= 60"));
        assertTrue(query.contains("p.max_dist / p.created_duration_seconds * 3.6"));
        assertTrue(query.contains("BETWEEN 0.5 AND 200"),
                "implausibly long and short timestamp spans must be rejected");
        assertTrue(query.contains("ORDER BY distance DESC NULLS LAST, id DESC"),
                "the fallback must use the average at the furthest point, not its peak value");
        assertTrue(query.contains("FILTER (WHERE average_speed > 0))[1] AS final_average_speed"));
        assertTrue(query.contains("p.final_average_speed BETWEEN 0.5 AND 200"));
        assertTrue(query.contains("THEN p.max_dist / p.final_average_speed * 3.6"));
        assertTrue(query.contains("ELSE NULL"),
                "a session without a credible duration must not dilute the user average");
        assertFalse(query.contains("MAX(average_speed)"));
    }

    @Test
    void sessionTrackPointsUseSameSessionAwareTimelineForElapsedSecondsAndOrdering() throws Exception {
        String query = queryFor("findSessionTrackPoints");

        assertTrue(query.contains("MAX(received_at) OVER () - MIN(received_at) OVER ()"));
        assertTrue(query.contains("THEN COALESCE(received_at, created_at)"));
        assertTrue(query.contains("ELSE COALESCE(created_at, received_at)"));
        assertTrue(query.contains("event_at - MIN(event_at) OVER ()"));
        assertTrue(query.contains("ORDER BY event_at ASC"));
    }

    private String queryFor(String methodName) throws Exception {
        Method method = switch (methodName) {
            case "findRecentSessionsWithStats" -> TrackingSessionRepository.class.getDeclaredMethod(
                    methodName, Integer.class, String.class, int.class, int.class);
            case "countSessionsFiltered" -> TrackingSessionRepository.class.getDeclaredMethod(
                    methodName, Integer.class, String.class);
            case "findSessionDetail", "findSessionTrackPoints" ->
                    TrackingSessionRepository.class.getDeclaredMethod(methodName, String.class);
            default -> TrackingSessionRepository.class.getDeclaredMethod(methodName, Integer.class);
        };
        return method.getAnnotation(Query.class).value();
    }
}
