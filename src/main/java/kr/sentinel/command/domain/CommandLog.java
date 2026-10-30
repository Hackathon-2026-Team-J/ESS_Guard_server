package kr.sentinel.command.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(indexes = {
        @Index(name = "idx_command_log_processed_issued_at", columnList = "processed, issued_at"),
        @Index(name = "idx_command_log_client_id_issued_at", columnList = "client_id, issued_at"),
        @Index(name = "idx_command_log_device_id_issued_at", columnList = "device_id, issued_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommandLog {

    private static final int WRITE_SINGLE_REGISTER = 6;
    private static final int WRITE_MULTIPLE_REGISTERS = 16;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "client_id", nullable = false, length = 50)
    private String clientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientRole role;

    @Column(name = "device_id", nullable = false, length = 50)
    private String deviceId;

    @Column(name = "function_code", nullable = false)
    private int functionCode;

    @Column(name = "register_address", nullable = false)
    private int registerAddress;

    @Column(name = "command_value", nullable = false)
    private int value;

    @Column(name = "command_kw", nullable = false)
    private int commandKw;

    @Enumerated(EnumType.STRING)
    @Column(name = "ess_state")
    private EssState essState;

    @Column(name = "labeled_anomaly", nullable = false)
    private boolean labeledAnomaly;

    private boolean processed;

    @Builder(access = AccessLevel.PRIVATE)
    private CommandLog(
            LocalDateTime issuedAt,
            String clientId,
            ClientRole role,
            String deviceId,
            int functionCode,
            int registerAddress,
            int value,
            int commandKw,
            EssState essState,
            boolean labeledAnomaly
    ) {
        this.issuedAt = issuedAt;
        this.clientId = clientId;
        this.role = role;
        this.deviceId = deviceId;
        this.functionCode = functionCode;
        this.registerAddress = registerAddress;
        this.value = value;
        this.commandKw = commandKw;
        this.essState = essState;
        this.labeledAnomaly = labeledAnomaly;
        this.processed = false;
    }

    public static CommandLog create(
            LocalDateTime issuedAt,
            String clientId,
            ClientRole role,
            String deviceId,
            int functionCode,
            int registerAddress,
            int value,
            int commandKw,
            EssState essState,
            boolean labeledAnomaly
    ) {
        return CommandLog.builder()
                .issuedAt(issuedAt)
                .clientId(clientId)
                .role(role)
                .deviceId(deviceId)
                .functionCode(functionCode)
                .registerAddress(registerAddress)
                .value(value)
                .commandKw(commandKw)
                .essState(essState)
                .labeledAnomaly(labeledAnomaly)
                .build();
    }

    public void markProcessed() {
        this.processed = true;
    }

    public void resetProcessed() {
        this.processed = false;
    }

    public boolean isWrite() {
        return functionCode == WRITE_SINGLE_REGISTER || functionCode == WRITE_MULTIPLE_REGISTERS;
    }

    public Direction getDirection() {
        return Direction.from(commandKw);
    }
}
