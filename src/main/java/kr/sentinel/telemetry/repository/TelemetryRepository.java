package kr.sentinel.telemetry.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.sentinel.telemetry.domain.Telemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TelemetryRepository extends JpaRepository<Telemetry, Long> {

    Optional<Telemetry> findFirstByProcessedFalseOrderByMeasuredAtAscIdAsc();

    Optional<Telemetry> findFirstByDeviceIdAndMeasuredAtBeforeOrderByMeasuredAtDesc(
            String deviceId,
            LocalDateTime measuredAt
    );

    @Modifying(clearAutomatically = true)
    @Query("update Telemetry t set t.processed = false")
    void resetAllProcessed();
}
