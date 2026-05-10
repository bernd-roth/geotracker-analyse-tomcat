package at.co.netconsulting.analyse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "gps_tracking_points")
@Getter
@Setter
@NoArgsConstructor
public class GpsTrackingPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private TrackingSession session;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal altitude;

    @Column(name = "horizontal_accuracy")
    private BigDecimal horizontalAccuracy;

    @Column(name = "vertical_accuracy_meters")
    private BigDecimal verticalAccuracyMeters;

    @Column(name = "number_of_satellites")
    private Integer numberOfSatellites;

    @Column(name = "used_number_of_satellites")
    private Integer usedNumberOfSatellites;

    @Column(name = "current_speed")
    private BigDecimal currentSpeed;

    @Column(name = "average_speed")
    private BigDecimal averageSpeed;

    @Column(name = "max_speed")
    private BigDecimal maxSpeed;

    @Column(name = "moving_average_speed")
    private BigDecimal movingAverageSpeed;

    private BigDecimal speed;

    @Column(name = "speed_accuracy_meters_per_second")
    private BigDecimal speedAccuracyMetersPerSecond;

    private BigDecimal distance;

    @Column(name = "covered_distance")
    private BigDecimal coveredDistance;

    @Column(name = "cumulative_elevation_gain")
    private BigDecimal cumulativeElevationGain;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "heart_rate_device_id")
    private HeartRateDevice heartRateDevice;

    private Integer lap;

    @Column(name = "received_at")
    private OffsetDateTime receivedAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    private BigDecimal temperature;

    @Column(name = "wind_speed")
    private BigDecimal windSpeed;

    @Column(name = "wind_direction")
    private BigDecimal windDirection;

    private Integer humidity;

    @Column(name = "weather_timestamp")
    private Long weatherTimestamp;

    @Column(name = "weather_code")
    private Integer weatherCode;

    private BigDecimal pressure;

    @Column(name = "pressure_accuracy")
    private Integer pressureAccuracy;

    @Column(name = "altitude_from_pressure")
    private BigDecimal altitudeFromPressure;

    @Column(name = "sea_level_pressure")
    private BigDecimal seaLevelPressure;

    private BigDecimal slope;

    @Column(name = "average_slope")
    private BigDecimal averageSlope;

    @Column(name = "max_uphill_slope")
    private BigDecimal maxUphillSlope;

    @Column(name = "max_downhill_slope")
    private BigDecimal maxDownhillSlope;
}
