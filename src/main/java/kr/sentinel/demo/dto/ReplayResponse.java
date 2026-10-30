package kr.sentinel.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ReplayResponse(
        @Schema(description = "재생 진행 여부", example = "true")
        boolean running,
        @Schema(description = "재생 처리된 명령 수", example = "120")
        long processedCount
) {

    public static ReplayResponse of(
            boolean running,
            long processedCount
    ) {
        return ReplayResponse.builder()
                .running(running)
                .processedCount(processedCount)
                .build();
    }
}
