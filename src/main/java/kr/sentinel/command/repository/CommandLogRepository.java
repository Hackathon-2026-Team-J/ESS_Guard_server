package kr.sentinel.command.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import kr.sentinel.command.domain.CommandLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CommandLogRepository extends JpaRepository<CommandLog, Long> {

    Optional<CommandLog> findFirstByProcessedFalseOrderByIdAsc();

    long countByProcessedTrue();

    long countByClientIdAndFunctionCodeInAndIssuedAtBetween(
            String clientId,
            Collection<Integer> functionCodes,
            LocalDateTime from,
            LocalDateTime to
    );

    @Modifying(clearAutomatically = true)
    @Query("update CommandLog c set c.processed = false")
    void resetAllProcessed();
}
