package at.co.netconsulting.analyse.dto;

public record LapSummary(
        String sessionId,
        String eventName,
        String userName,
        int lapNumber,
        String duration,
        double distanceKm,
        String sportType,
        String pace
) {}
