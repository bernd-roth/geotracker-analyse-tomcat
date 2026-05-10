package at.co.netconsulting.analyse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tracking_data")
@Getter
@Setter
@NoArgsConstructor
public class TrackingData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    private String person;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal altitude;

    @Column(name = "horizontal_accuracy")
    private BigDecimal horizontalAccuracy;

    @Column(name = "vertical_accuracy_meters")
    private BigDecimal verticalAccuracyMeters;

    @Column(name = "number_of_satellites")
    private Integer numberOfSatellites;

    private Integer satellites;

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

    @Column(name = "heart_rate_device")
    private String heartRateDevice;

    private Integer lap;

    @Column(name = "start_date_time")
    private OffsetDateTime startDateTime;

    @Column(name = "received_at")
    private OffsetDateTime receivedAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    private String firstname;
    private String lastname;
    private String birthdate;
    private BigDecimal height;
    private BigDecimal weight;

    @Column(name = "min_distance_meters")
    private Integer minDistanceMeters;

    @Column(name = "min_time_seconds")
    private Integer minTimeSeconds;

    @Column(name = "voice_announcement_interval")
    private Integer voiceAnnouncementInterval;

    @Column(name = "event_name")
    private String eventName;

    @Column(name = "sport_type")
    private String sportType;

    private String comment;
    private String clothing;
}
