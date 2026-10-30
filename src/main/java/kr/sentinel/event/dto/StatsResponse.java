package kr.sentinel.event.dto;

import java.util.Map;
import kr.sentinel.detection.Severity;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record StatsResponse(
        long processedCount,
        long totalCount,
        long detectionCount,
        long labeledAnomalyCount,
        Map<Severity, Long> severityCounts,
        Map<String, Long> ruleCounts
) {

    public static StatsResponse of(
            long processedCount,
            long totalCount,
            long detectionCount,
            long labeledAnomalyCount,
            Map<Severity, Long> severityCounts,
            Map<String, Long> ruleCounts
    ) {
        return StatsResponse.builder()
                .processedCount(processedCount)
                .totalCount(totalCount)
                .detectionCount(detectionCount)
                .labeledAnomalyCount(labeledAnomalyCount)
                .severityCounts(severityCounts)
                .ruleCounts(ruleCounts)
                .build();
    }
}
