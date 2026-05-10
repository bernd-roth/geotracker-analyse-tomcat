package at.co.netconsulting.analyse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Table(name = "heart_rate_devices")
@Getter
@Setter
@NoArgsConstructor
public class HeartRateDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_id")
    private Integer deviceId;

    @Column(name = "device_name", nullable = false, unique = true)
    private String deviceName;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
