package at.co.netconsulting.analyse.controller;

import at.co.netconsulting.analyse.dto.*;
import at.co.netconsulting.analyse.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @GetMapping("/")
    public String dashboard(@RequestParam(value = "userId", required = false) Integer userId,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            Model model) {
        model.addAttribute("allUsers", analysisService.getAllUsers());
        model.addAttribute("selectedUserId", userId);

        long totalSessionCount = analysisService.countSessions(userId);
        int pageSize = AnalysisService.SESSIONS_PAGE_SIZE;
        int totalPages = (int) Math.max(1, (totalSessionCount + pageSize - 1) / pageSize);
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;
        model.addAttribute("sessionsPage", page);
        model.addAttribute("sessionsTotalPages", totalPages);
        model.addAttribute("sessionsTotalCount", totalSessionCount);
        model.addAttribute("sessionsPageSize", pageSize);

        Map<String, Object> overall = analysisService.getOverallStats(userId);
        model.addAttribute("totalSessions", overall.get("totalSessions"));
        model.addAttribute("activeUsers", overall.get("activeUsers"));
        model.addAttribute("totalDistanceKm", overall.get("totalDistanceKm"));
        model.addAttribute("totalGpsPoints", overall.get("totalGpsPoints"));

        List<SportTypeStats> sportStats = analysisService.getSportTypeStats(userId);
        model.addAttribute("sportTypeStats", sportStats);

        // Chart data for sport type pie chart
        model.addAttribute("sportLabels", sportStats.stream().map(SportTypeStats::sportType).toList());
        model.addAttribute("sportCounts", sportStats.stream().map(SportTypeStats::sessionCount).toList());

        List<UserStats> userStats = analysisService.getUserStats(userId);
        model.addAttribute("userStats", userStats);

        List<MonthlyStats> monthlyStats = analysisService.getMonthlyStats(userId);
        model.addAttribute("monthlyStats", monthlyStats);

        // Chart data for monthly trend (reverse for chronological order)
        List<MonthlyStats> monthlyReversed = monthlyStats.reversed();
        model.addAttribute("monthLabels", monthlyReversed.stream().map(MonthlyStats::yearMonth).toList());
        model.addAttribute("monthSessions", monthlyReversed.stream().map(MonthlyStats::sessionCount).toList());
        model.addAttribute("monthDistances", monthlyReversed.stream().map(MonthlyStats::totalDistanceKm).toList());

        model.addAttribute("recentSessions", analysisService.getRecentSessions(userId, page));
        model.addAttribute("upcomingEvents", analysisService.getUpcomingEvents());
        model.addAttribute("allPlannedEvents", analysisService.getAllPlannedEvents());
        model.addAttribute("records", analysisService.getRecords(userId));
        model.addAttribute("fastestPaces", analysisService.getFastestSessionPaces(userId));

        return "dashboard";
    }

    @GetMapping("/session/{sessionId}")
    public String sessionDetail(@PathVariable String sessionId, Model model) {
        Optional<SessionDetail> detail = analysisService.getSessionDetail(sessionId);
        if (detail.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("sessionDetail", detail.get());
        model.addAttribute("laps", analysisService.getSessionLaps(sessionId));
        return "session-detail";
    }

    @GetMapping("/api/session/{sessionId}/track")
    @ResponseBody
    public List<TrackPoint> sessionTrack(@PathVariable String sessionId) {
        return analysisService.getSessionTrackPoints(sessionId);
    }
}
