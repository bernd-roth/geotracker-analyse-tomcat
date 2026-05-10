package at.co.netconsulting.analyse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tracking_sessions")
@Getter
@Setter
@NoArgsConstructor
public class TrackingSession {

    @Id
    @Column(name = "session_id")
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "event_name")
    private String eventName;

    @Column(name = "sport_type")
    private String sportType;

    private String comment;
    private String clothing;

    @Column(name = "start_date_time")
    private OffsetDateTime startDateTime;

    @Column(name = "min_distance_meters")
    private Integer minDistanceMeters;

    @Column(name = "min_time_seconds")
    private Integer minTimeSeconds;

    @Column(name = "voice_announcement_interval")
    private Integer voiceAnnouncementInterval;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "start_city")
    private String startCity;

    @Column(name = "start_country")
    private String startCountry;

    @Column(name = "start_address")
    private String startAddress;

    @Column(name = "end_city")
    private String endCity;

    @Column(name = "end_country")
    private String endCountry;

    @Column(name = "end_address")
    private String endAddress;

    @Column(name = "app_version")
    private String appVersion;
}
