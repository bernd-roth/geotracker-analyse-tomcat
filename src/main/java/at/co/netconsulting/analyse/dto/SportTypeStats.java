package at.co.netconsulting.analyse.dto;

public record SportTypeStats(
        String sportType,
        long sessionCount,
        double totalDistanceKm,
        double avgDistanceKm
) {}
