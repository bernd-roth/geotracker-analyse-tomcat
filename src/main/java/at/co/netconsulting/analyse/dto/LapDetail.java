package at.co.netconsulting.analyse.dto;

public record LapDetail(
        int lapNumber,
        long durationMs,
        String durationFormatted,
        double distanceM,
        String paceFormatted
) {}
