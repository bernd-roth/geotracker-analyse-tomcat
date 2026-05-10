package at.co.netconsulting.analyse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Table(name = "discipline_transitions")
@Getter
@Setter
@NoArgsConstructor
public class DisciplineTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transition_id")
    private Integer transitionId;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "event_name")
    private String eventName;

    @Column(name = "discipline_name", nullable = false)
    private String disciplineName;

    @Column(name = "transition_number", nullable = false)
    private Integer transitionNumber;

    @Column(name = "transition_timestamp", nullable = false)
    private Long transitionTimestamp;

    @Column(name = "received_at")
    private OffsetDateTime receivedAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
