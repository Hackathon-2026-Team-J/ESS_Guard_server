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

    @Operation(summary = "명령 재생 시작")
    @PostMapping("/start")
    public ReplayResponse start() {
        replayService.start();

        return createResponse();
    }

    @Operation(summary = "명령 재생 정지")
    @PostMapping("/stop")
    public ReplayResponse stop() {
        replayService.stop();

        return createResponse();
    }

    @Operation(summary = "재생 상태와 탐지 이벤트 초기화")
    @PostMapping("/reset")
    public ReplayResponse reset() {
        replayService.reset();

        return createResponse();
    }

    private ReplayResponse createResponse() {
        return ReplayResponse.of(replayService.isRunning(), replayService.countProcessed());
    }
}
