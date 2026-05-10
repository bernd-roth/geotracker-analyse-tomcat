package at.co.netconsulting.analyse.dto;

public record MonthlyStats(
        String yearMonth,
        long sessionCount,
        double totalDistanceKm
) {}
