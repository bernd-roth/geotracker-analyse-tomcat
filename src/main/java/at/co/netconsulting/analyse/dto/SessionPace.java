package at.co.netconsulting.analyse.dto;

public record SessionPace(
        String sessionId,
        String eventName,
        String userName,
        String sportType,
        String date,
        String location,
        double distanceKm,
        String duration,
        String pace
) {}
