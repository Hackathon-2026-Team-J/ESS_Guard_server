package kr.sentinel.demo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.sentinel.command.ReplayService;
import kr.sentinel.demo.dto.ReplayResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Replay", description = "시연용 명령 재생 제어 API")
@RestController
@RequestMapping("/api/replay")
@RequiredArgsConstructor
public class ReplayController {

    private final ReplayService replayService;

    @Operation(summary = "명령 재생 시작", description = "미처리 명령을 1초마다 1건씩 처리하고, 모두 처리하면 자동으로 멈춘다.")
    @PostMapping("/start")
    public ReplayResponse start() {
        replayService.start();

        return createResponse();
    }

    @Operation(summary = "명령 재생 정지", description = "재생을 멈춘다. 처리 위치는 유지되어 다시 시작하면 이어서 처리한다.")
    @PostMapping("/stop")
    public ReplayResponse stop() {
        replayService.stop();

        return createResponse();
    }

    @Operation(
            summary = "재생 상태와 탐지 이벤트 초기화",
            description = "재생을 멈추고 탐지 이벤트를 모두 삭제한 뒤 명령 처리 상태를 되돌린다. SSE 구독자에게 `reset` 이벤트를 보낸다."
    )
    @PostMapping("/reset")
    public ReplayResponse reset() {
        replayService.reset();

        return createResponse();
    }

    private ReplayResponse createResponse() {
        return ReplayResponse.of(replayService.isRunning(), replayService.countProcessed());
    }
}
