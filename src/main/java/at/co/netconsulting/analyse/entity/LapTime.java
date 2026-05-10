package at.co.netconsulting.analyse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "lap_times")
@Getter
@Setter
@NoArgsConstructor
public class LapTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "lap_number", nullable = false)
    private Integer lapNumber;

    @Column(name = "start_time", nullable = false)
    private Long startTime;

    @Column(name = "end_time", nullable = false)
    private Long endTime;

    private Long duration;

    @Column(nullable = false)
    private BigDecimal distance;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
