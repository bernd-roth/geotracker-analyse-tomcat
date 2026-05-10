package at.co.netconsulting.analyse.dto;

public record RecordEntry(
        String recordType,
        String eventName,
        String userName,
        String sportType,
        double value,
        String unit,
        String dateFormatted
) {}
