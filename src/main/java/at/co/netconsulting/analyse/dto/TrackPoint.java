package at.co.netconsulting.analyse.dto;

public record TrackPoint(
        double lat,
        double lon,
        Double altitude,
        Integer heartRate,
        double distanceM,
        double speedKmh,
        long secondsFromStart,
        Double temperature,
        Double windSpeed,
        Double windDirection,
        Integer humidity,
        Integer weatherCode,
        Double pressure,
        Double seaLevelPressure,
        Double altitudeFromPressure,
        Double slope,
        Double horizontalAccuracy,
        Integer numberOfSatellites
) {}
