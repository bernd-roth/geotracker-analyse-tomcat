package at.co.netconsulting.analyse.dto;

public record UserStats(
        String userName,
        long sessionCount,
        double totalDistanceKm,
        double avgSpeedKmh,
        double totalElevationGainM,
        double avgHeartRate
) {}
