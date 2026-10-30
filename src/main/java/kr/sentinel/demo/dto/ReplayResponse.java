package kr.sentinel.demo.dto;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ReplayResponse(
        boolean running,
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
