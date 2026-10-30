package kr.sentinel.plan.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.sentinel.plan.domain.DispatchPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispatchPlanRepository extends JpaRepository<DispatchPlan, Long> {

    Optional<DispatchPlan> findFirstByDeviceIdAndStartAtLessThanEqualAndEndAtGreaterThan(
            String deviceId,
            LocalDateTime startAtLimit,
            LocalDateTime endAtLimit
    );

    List<DispatchPlan> findAllByDeviceIdAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
            String deviceId,
            LocalDateTime to,
            LocalDateTime from
    );
}
