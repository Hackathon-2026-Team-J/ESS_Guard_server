package kr.sentinel.stream;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.sentinel.event.dto.EventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Stream", description = "실시간 탐지 스트림 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StreamController {

    private final SseHub sseHub;

    @Operation(
            summary = "탐지 이벤트 SSE 구독",
            description = "`detection` 이벤트로 탐지 결과를 JSON으로 받고, 재생을 초기화하면 `reset` 이벤트를 받는다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "SSE 연결 성공",
            content = @Content(
                    mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                    schema = @Schema(implementation = EventResponse.class)
            )
    )
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return sseHub.subscribe();
    }
}
