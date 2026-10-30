package kr.sentinel.event;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import java.util.List;
import kr.sentinel.event.dto.EventResponse;
import kr.sentinel.event.dto.StatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Event", description = "탐지 이벤트 조회 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @Operation(summary = "최근 탐지 이벤트 조회", description = "탐지 이벤트를 최신순으로 조회한다.")
    @GetMapping("/events")
    public List<EventResponse> getEvents(
            @Parameter(description = "조회 개수 (1 이상)", example = "50")
            @RequestParam(defaultValue = "50") @Min(1) int limit
    ) {
        return eventService.getEvents(limit);
    }

    @Operation(summary = "탐지 통계 조회", description = "재생 진행 건수와 심각도별, 규칙별 탐지 건수를 조회한다.")
    @GetMapping("/stats")
    public StatsResponse getStats() {
        return eventService.getStats();
    }
}
