package kr.sentinel.detection;

public record Detection(String ruleId, Severity severity, String message) {
}
