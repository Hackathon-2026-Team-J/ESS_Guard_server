package kr.sentinel.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import kr.sentinel.command.domain.ClientRole;
import kr.sentinel.command.domain.CommandLog;
import kr.sentinel.command.domain.EssState;
import kr.sentinel.command.repository.CommandLogRepository;
import kr.sentinel.plan.domain.DispatchPlan;
import kr.sentinel.plan.repository.DispatchPlanRepository;
import kr.sentinel.telemetry.domain.Telemetry;
import kr.sentinel.telemetry.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandLogLoader implements ApplicationRunner {

    private static final String COMMAND_CSV_PATH = "data/ems_commands.csv";
    private static final String PLAN_CSV_PATH = "data/dispatch_plan.csv";
    private static final String TELEMETRY_CSV_PATH = "data/telemetry.csv";
    private static final int HEADER_LINES = 1;
    private static final String DELIMITER = ",";

    private final CommandLogRepository commandLogRepository;
    private final DispatchPlanRepository dispatchPlanRepository;
    private final TelemetryRepository telemetryRepository;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        load(COMMAND_CSV_PATH, commandLogRepository, this::parseCommandLog);
        load(PLAN_CSV_PATH, dispatchPlanRepository, this::parseDispatchPlan);
        load(TELEMETRY_CSV_PATH, telemetryRepository, this::parseTelemetry);
    }

    private <T> void load(
            String path,
            JpaRepository<T, Long> repository,
            Function<String[], T> parser
    ) throws IOException {
        if (repository.count() > 0) {
            return;
        }

        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            log.warn("목 데이터 파일이 없어 건너뜀: {}", path);
            return;
        }

        List<T> entities = readEntities(resource, parser);
        repository.saveAll(entities);

        log.info("목 데이터 {} {}건 적재", path, entities.size());
    }

    private <T> List<T> readEntities(
            ClassPathResource resource,
            Function<String[], T> parser
    ) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .skip(HEADER_LINES)
                    .map(line -> line.split(DELIMITER))
                    .map(parser)
                    .toList();
        }
    }

    private CommandLog parseCommandLog(String[] columns) {
        return CommandLog.create(
                LocalDateTime.parse(columns[0]),
                columns[1],
                ClientRole.valueOf(columns[2]),
                columns[3],
                Integer.parseInt(columns[4]),
                Integer.parseInt(columns[5]),
                Integer.parseInt(columns[6]),
                Integer.parseInt(columns[7]),
                EssState.valueOf(columns[8]),
                Boolean.parseBoolean(columns[9])
        );
    }

    private DispatchPlan parseDispatchPlan(String[] columns) {
        return DispatchPlan.create(
                columns[0],
                LocalDateTime.parse(columns[1]),
                LocalDateTime.parse(columns[2]),
                Integer.parseInt(columns[3])
        );
    }

    private Telemetry parseTelemetry(String[] columns) {
        return Telemetry.create(
                columns[0],
                LocalDateTime.parse(columns[1]),
                Integer.parseInt(columns[2]),
                Double.parseDouble(columns[3]),
                Boolean.parseBoolean(columns[4])
        );
    }
}
