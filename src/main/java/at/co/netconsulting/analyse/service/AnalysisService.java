package at.co.netconsulting.analyse.service;

import at.co.netconsulting.analyse.dto.*;
import at.co.netconsulting.analyse.entity.PlannedEvent;
import at.co.netconsulting.analyse.entity.User;
import at.co.netconsulting.analyse.repository.LapTimeRepository;
import at.co.netconsulting.analyse.repository.PlannedEventRepository;
import at.co.netconsulting.analyse.repository.TrackingSessionRepository;
import at.co.netconsulting.analyse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(ZoneId.systemDefault());

    private final TrackingSessionRepository sessionRepository;
    private final PlannedEventRepository plannedEventRepository;
    private final LapTimeRepository lapTimeRepository;
    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByFirstnameAsc();
    }

    public Map<String, Object> getOverallStats(Integer userId) {
        List<Object[]> rows = sessionRepository.findOverallStats(userId);
        if (rows.isEmpty()) {
            return Map.of("totalSessions", 0L, "activeUsers", 0L,
                    "totalDistanceKm", 0.0, "totalGpsPoints", 0L);
        }
        Object[] r = rows.getFirst();
        return Map.of(
                "totalSessions", toLong(r[0]),
                "activeUsers", toLong(r[1]),
                "totalDistanceKm", toDouble(r[2]),
                "totalGpsPoints", toLong(r[3])
        );
    }

    public List<SportTypeStats> getSportTypeStats(Integer userId) {
        return sessionRepository.findSportTypeStats(userId).stream()
                .map(r -> new SportTypeStats(
                        r[0] != null ? r[0].toString() : "Unknown",
                        toLong(r[1]),
                        toDouble(r[2]),
                        toDouble(r[3])
                ))
                .toList();
    }

    public List<UserStats> getUserStats(Integer userId) {
        return sessionRepository.findUserStats(userId).stream()
                .map(r -> new UserStats(
                        r[0] != null ? r[0].toString().trim() : "Unknown",
                        toLong(r[1]),
                        toDouble(r[2]),
                        toDouble(r[3]),
                        toDouble(r[4]),
                        toDouble(r[5])
                ))
                .toList();
    }

    public List<MonthlyStats> getMonthlyStats(Integer userId) {
        return sessionRepository.findMonthlyStats(userId).stream()
                .map(r -> new MonthlyStats(
                        r[0] != null ? r[0].toString() : "",
                        toLong(r[1]),
                        toDouble(r[2])
                ))
                .toList();
    }

    public static final int SESSIONS_PAGE_SIZE = 30;

    public long countSessions(Integer userId) {
        return sessionRepository.countSessions(userId);
    }

    public long countSessions(Integer userId, String search) {
        if (search == null || search.isBlank()) {
            return sessionRepository.countSessions(userId);
        }
        return sessionRepository.countSessionsFiltered(userId, search);
    }

    public List<SessionSummary> getRecentSessions(Integer userId, int page, String search) {
        int offset = Math.max(0, page) * SESSIONS_PAGE_SIZE;
        String normalizedSearch = (search != null && !search.isBlank()) ? search : null;
        return sessionRepository.findRecentSessionsWithStats(userId, normalizedSearch, SESSIONS_PAGE_SIZE, offset).stream()
                .map(r -> {
                    double distKm = toDouble(r[8]);
                    double durationSec = toDouble(r[14]);
                    return new SessionSummary(
                            str(r[0]),
                            str(r[1]),
                            str(r[2]),
                            formatDateTime(r[3]),
                            str(r[4]),
                            str(r[5]),
                            str(r[6]),
                            str(r[7]),
                            distKm,
                            toDouble(r[9]),
                            toDouble(r[10]),
                            toDouble(r[11]),
                            r[12] != null ? toDouble(r[12]) : null,
                            r[13] != null ? toDouble(r[13]) : null,
                            formatDuration(r[14]),
                            formatPaceFromSeconds(durationSec, distKm)
                    );
                })
                .toList();
    }

    public List<PlannedEvent> getUpcomingEvents() {
        return plannedEventRepository
                .findByPlannedEventDateGreaterThanEqualOrderByPlannedEventDateAsc(LocalDate.now());
    }

    public List<PlannedEvent> getAllPlannedEvents() {
        return plannedEventRepository.findAllByOrderByPlannedEventDateDesc();
    }

    public List<RecordEntry> getRecords(Integer userId) {
        List<RecordEntry> records = new ArrayList<>();
        addRecord(records, sessionRepository.findLongestDistanceRecord(userId));
        addRecord(records, sessionRepository.findHighestElevationRecord(userId));
        addRecord(records, sessionRepository.findFastestRunRecord(userId));
        return records;
    }

    public Optional<SessionDetail> getSessionDetail(String sessionId) {
        List<Object[]> rows = sessionRepository.findSessionDetail(sessionId);
        if (rows.isEmpty()) return Optional.empty();
        Object[] r = rows.getFirst();
        double distKm = toDouble(r[13]);
        double durationSec = toDouble(r[21]);
        Double avgHr = r[17] != null ? toDouble(r[17]) : null;
        Integer minHr = r[18] != null ? ((Number) r[18]).intValue() : null;
        Integer maxHr = r[19] != null ? ((Number) r[19]).intValue() : null;
        return Optional.of(new SessionDetail(
                str(r[0]),
                str(r[1]),
                str(r[2]),
                formatDateTime(r[3]),
                str(r[4]),
                str(r[5]),
                str(r[6]),
                str(r[7]),
                str(r[8]),
                str(r[9]),
                str(r[10]),
                str(r[11]),
                str(r[12]),
                distKm,
                toDouble(r[14]),
                toDouble(r[15]),
                toDouble(r[16]),
                avgHr,
                minHr,
                maxHr,
                r[20] != null ? toDouble(r[20]) : null,
                formatDuration(durationSec),
                formatPaceFromSeconds(durationSec, distKm),
                avgHr != null
        ));
    }

    public List<TrackPoint> getSessionTrackPoints(String sessionId) {
        return sessionRepository.findSessionTrackPoints(sessionId).stream()
                .map(r -> new TrackPoint(
                        toDouble(r[0]),
                        toDouble(r[1]),
                        r[2] != null ? toDouble(r[2]) : null,
                        r[3] != null ? ((Number) r[3]).intValue() : null,
                        toDouble(r[4]),
                        toDouble(r[5]),
                        (long) toDouble(r[6]),
                        r[7] != null ? toDouble(r[7]) : null,
                        r[8] != null ? toDouble(r[8]) : null,
                        r[9] != null ? toDouble(r[9]) : null,
                        r[10] != null ? ((Number) r[10]).intValue() : null,
                        r[11] != null ? ((Number) r[11]).intValue() : null,
                        r[12] != null ? toDouble(r[12]) : null,
                        r[13] != null ? toDouble(r[13]) : null,
                        r[14] != null ? toDouble(r[14]) : null,
                        r[15] != null ? toDouble(r[15]) : null,
                        r[16] != null ? toDouble(r[16]) : null,
                        r[17] != null ? ((Number) r[17]).intValue() : null
                ))
                .toList();
    }

    public List<LapDetail> getSessionLaps(String sessionId) {
        return lapTimeRepository.findBySessionIdOrderByLapNumberAsc(sessionId).stream()
                .map(l -> {
                    long durMs = l.getDuration() != null ? l.getDuration() : 0L;
                    double distM = l.getDistance() != null ? l.getDistance().doubleValue() : 0.0;
                    String pace = distM > 0 && durMs > 0
                            ? formatPaceFromSeconds(durMs / 1000.0, distM / 1000.0)
                            : "-";
                    return new LapDetail(
                            l.getLapNumber(),
                            durMs,
                            formatMillis(durMs),
                            distM,
                            pace
                    );
                })
                .toList();
    }

    public List<SessionPace> getFastestSessionPaces(Integer userId) {
        return sessionRepository.findFastestSessionPaces(userId).stream()
                .map(r -> {
                    double distKm = toDouble(r[7]);
                    double durationSec = toDouble(r[8]);
                    String location = str(r[5]);
                    String country = str(r[6]);
                    if (!country.isEmpty()) {
                        location = location.isEmpty() ? country : location + ", " + country;
                    }
                    return new SessionPace(
                            str(r[0]),
                            str(r[1]),
                            str(r[2]),
                            str(r[3]),
                            formatDate(r[4]),
                            location,
                            distKm,
                            formatDuration(r[8]),
                            formatPaceFromSeconds(durationSec, distKm)
                    );
                })
                .toList();
    }

    private void addRecord(List<RecordEntry> records, List<Object[]> rows) {
        if (!rows.isEmpty()) {
            Object[] r = rows.getFirst();
            records.add(new RecordEntry(
                    str(r[0]), str(r[1]), str(r[2]), str(r[3]),
                    toDouble(r[4]), str(r[5]),
                    formatDate(r[6])
            ));
        }
    }

    private String formatDateTime(Object val) {
        Instant instant = toInstant(val);
        return instant != null ? DATE_TIME_FMT.format(instant) : "-";
    }

    private String formatDate(Object val) {
        Instant instant = toInstant(val);
        return instant != null ? DATE_FMT.format(instant) : "-";
    }

    private String formatDuration(Object durationSeconds) {
        if (durationSeconds == null) return "-";
        long total = ((Number) durationSeconds).longValue();
        if (total <= 0) return "-";
        long hours = total / 3600;
        long minutes = (total % 3600) / 60;
        long secs = total % 60;
        if (hours > 0) {
            return String.format("%dh %02dm %02ds", hours, minutes, secs);
        }
        return String.format("%dm %02ds", minutes, secs);
    }

    private String formatMillis(long millis) {
        if (millis <= 0) return "-";
        long totalSecs = millis / 1000;
        long hours = totalSecs / 3600;
        long minutes = (totalSecs % 3600) / 60;
        long secs = totalSecs % 60;
        if (hours > 0) {
            return String.format("%dh %02dm %02ds", hours, minutes, secs);
        }
        if (minutes > 0) {
            return String.format("%dm %02ds", minutes, secs);
        }
        return String.format("%ds", secs);
    }

    private String formatPaceFromSeconds(double durationSeconds, double distanceKm) {
        if (durationSeconds <= 0 || distanceKm <= 0) return "-";
        double paceMinPerKm = (durationSeconds / 60.0) / distanceKm;
        int paceMin = (int) paceMinPerKm;
        int paceSec = (int) ((paceMinPerKm - paceMin) * 60);
        return String.format("%d:%02d min/km", paceMin, paceSec);
    }

    private static double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof BigDecimal bd) return bd.doubleValue();
        return ((Number) val).doubleValue();
    }

    private static long toLong(Object val) {
        if (val == null) return 0L;
        return ((Number) val).longValue();
    }

    private static Instant toInstant(Object val) {
        if (val == null) return null;
        if (val instanceof Instant i) return i;
        if (val instanceof java.sql.Timestamp ts) return ts.toInstant();
        return null;
    }

    private static String str(Object val) {
        return val != null ? val.toString() : "";
    }
}
