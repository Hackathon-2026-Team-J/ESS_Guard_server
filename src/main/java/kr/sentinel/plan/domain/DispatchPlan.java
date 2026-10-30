package kr.sentinel.plan.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import kr.sentinel.command.domain.Direction;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(indexes = @Index(name = "idx_dispatch_plan_device_id_start_at", columnList = "device_id, start_at"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DispatchPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false, length = 50)
    private String deviceId;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "planned_kw", nullable = false)
    private int plannedKw;

    @Builder(access = AccessLevel.PRIVATE)
    private DispatchPlan(
            String deviceId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            int plannedKw
    ) {
        this.deviceId = deviceId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.plannedKw = plannedKw;
    }

    public static DispatchPlan create(
            String deviceId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            int plannedKw
    ) {
        return DispatchPlan.builder()
                .deviceId(deviceId)
                .startAt(startAt)
                .endAt(endAt)
                .plannedKw(plannedKw)
                .build();
    }

    public Direction getDirection() {
        return Direction.from(plannedKw);
    }
}
