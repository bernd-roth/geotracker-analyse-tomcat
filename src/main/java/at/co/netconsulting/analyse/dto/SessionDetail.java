package at.co.netconsulting.analyse.dto;

public record SessionDetail(
        String sessionId,
        String eventName,
        String sportType,
        String startDateTimeFormatted,
        String startCity,
        String startCountry,
        String startAddress,
        String endCity,
        String endCountry,
        String endAddress,
        String comment,
        String clothing,
        String userName,
        double distanceKm,
        double avgSpeedKmh,
        double maxSpeedKmh,
        double elevationGainM,
        Double avgHeartRate,
        Integer minHeartRate,
        Integer maxHeartRate,
        Double avgTemperature,
        String duration,
        String pace,
        boolean hasHeartRate
) {}
