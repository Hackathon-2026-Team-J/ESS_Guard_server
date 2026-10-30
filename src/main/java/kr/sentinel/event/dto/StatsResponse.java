package kr.sentinel.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import kr.sentinel.detection.Severity;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record StatsResponse(
        @Schema(description = "재생 처리된 명령 수", example = "120")
        long processedCount,
        @Schema(description = "전체 명령 수", example = "230")
        long totalCount,
        @Schema(description = "탐지 이벤트 수", example = "5")
        long detectionCount,
        @Schema(description = "목 데이터에 이상으로 라벨링된 명령 수", example = "12")
        long labeledAnomalyCount,
        @Schema(description = "심각도별 탐지 건수", example = "{\"HIGH\": 5}")
        Map<Severity, Long> severityCounts,
        @Schema(description = "규칙 ID별 탐지 건수", example = "{\"R2\": 5}")
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
