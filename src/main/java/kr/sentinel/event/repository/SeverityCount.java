package kr.sentinel.event.repository;

import kr.sentinel.detection.Severity;

public interface SeverityCount {

    Severity getSeverity();

    long getCount();
}
