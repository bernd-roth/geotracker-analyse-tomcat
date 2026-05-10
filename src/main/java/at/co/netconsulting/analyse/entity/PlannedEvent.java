package at.co.netconsulting.analyse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "planned_events")
@Getter
@Setter
@NoArgsConstructor
public class PlannedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "planned_event_id")
    private Integer plannedEventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "planned_event_name", nullable = false)
    private String plannedEventName;

    @Column(name = "planned_event_date", nullable = false)
    private LocalDate plannedEventDate;

    @Column(name = "planned_event_type")
    private String plannedEventType;

    @Column(name = "planned_event_country", nullable = false)
    private String plannedEventCountry;

    @Column(name = "planned_event_city", nullable = false)
    private String plannedEventCity;

    @Column(name = "planned_latitude")
    private BigDecimal plannedLatitude;

    @Column(name = "planned_longitude")
    private BigDecimal plannedLongitude;

    @Column(name = "is_entered_and_finished")
    private Boolean isEnteredAndFinished;

    private String website;
    private String comment;

    @Column(name = "reminder_date_time")
    private OffsetDateTime reminderDateTime;

    @Column(name = "is_reminder_active")
    private Boolean isReminderActive;

    @Column(name = "is_recurring")
    private Boolean isRecurring;

    @Column(name = "recurring_type")
    private String recurringType;

    @Column(name = "recurring_interval")
    private Integer recurringInterval;

    @Column(name = "recurring_end_date")
    private LocalDate recurringEndDate;

    @Column(name = "recurring_days_of_week")
    private String recurringDaysOfWeek;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "created_by_user_id", nullable = false)
    private Integer createdByUserId;

    @Column(name = "is_public")
    private Boolean isPublic;
}
