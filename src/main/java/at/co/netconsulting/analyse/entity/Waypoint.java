package at.co.netconsulting.analyse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "waypoints")
@Getter
@Setter
@NoArgsConstructor
public class Waypoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waypoint_id")
    private Integer waypointId;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "event_name")
    private String eventName;

    @Column(name = "waypoint_name", nullable = false)
    private String waypointName;

    @Column(name = "waypoint_description")
    private String waypointDescription;

    @Column(nullable = false)
    private BigDecimal latitude;

    @Column(nullable = false)
    private BigDecimal longitude;

    private BigDecimal elevation;

    @Column(name = "waypoint_timestamp", nullable = false)
    private Long waypointTimestamp;

    @Column(name = "received_at")
    private OffsetDateTime receivedAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
