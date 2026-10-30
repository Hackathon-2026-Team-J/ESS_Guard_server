package kr.sentinel.event.repository;

import java.time.LocalDateTime;
import java.util.List;
import kr.sentinel.event.domain.DetectionEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DetectionEventRepository extends JpaRepository<DetectionEvent, Long> {

    List<DetectionEvent> findAllByOrderByIdDesc(Pageable pageable);

    List<DetectionEvent> findAllByCommandLogId(Long commandLogId);

    @Query("""
            select count(e)
            from DetectionEvent e
            join CommandLog c on e.commandLogId = c.id
            where c.deviceId = :deviceId and c.issuedAt between :from and :to
            """)
    long countCommandDetections(
            @Param("deviceId") String deviceId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("select e.severity as severity, count(e) as count from DetectionEvent e group by e.severity")
    List<SeverityCount> countGroupBySeverity();

    @Query("select e.ruleId as ruleId, count(e) as count from DetectionEvent e group by e.ruleId")
    List<RuleCount> countGroupByRuleId();
}
