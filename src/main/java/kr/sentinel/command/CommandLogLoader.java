package kr.sentinel.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import kr.sentinel.command.domain.ClientRole;
import kr.sentinel.command.domain.CommandLog;
import kr.sentinel.command.domain.EssState;
import kr.sentinel.command.repository.CommandLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandLogLoader implements ApplicationRunner {

    private static final String CSV_PATH = "data/ems_commands.csv";
    private static final int HEADER_LINES = 1;
    private static final String DELIMITER = ",";

    private final CommandLogRepository commandLogRepository;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (commandLogRepository.count() > 0) {
            return;
        }

        List<CommandLog> commandLogs = readCommandLogs();
        commandLogRepository.saveAll(commandLogs);

        log.info("EMS 명령 로그 {}건 적재", commandLogs.size());
    }

    private List<CommandLog> readCommandLogs() throws IOException {
        ClassPathResource resource = new ClassPathResource(CSV_PATH);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .skip(HEADER_LINES)
                    .map(this::parseLine)
                    .toList();
        }
    }

    private CommandLog parseLine(String line) {
        String[] columns = line.split(DELIMITER);

        return CommandLog.create(
                LocalDateTime.parse(columns[0]),
                columns[1],
                ClientRole.valueOf(columns[2]),
                columns[3],
                Integer.parseInt(columns[4]),
                Integer.parseInt(columns[5]),
                Integer.parseInt(columns[6]),
                EssState.valueOf(columns[7]),
                Boolean.parseBoolean(columns[8])
        );
    }
}
