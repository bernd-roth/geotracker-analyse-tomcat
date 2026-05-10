package at.co.netconsulting.analyse.dto;

public record SessionSummary(
        String sessionId,
        String eventName,
        String sportType,
        String startDateTimeFormatted,
        String startCity,
        String startCountry,
        String comment,
        String userName,
        double distanceKm,
        double avgSpeedKmh,
        double maxSpeedKmh,
        double elevationGainM,
        Double avgHeartRate,
        Double avgTemperature,
        String duration,
        String pace
) {}
