package kr.sentinel.command.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Direction {
    DISCHARGE("방전"),
    CHARGE("충전"),
    IDLE("대기");

    private final String label;

    public static Direction from(int kw) {
        if (kw > 0) {
            return DISCHARGE;
        }

        return kw < 0 ? CHARGE : IDLE;
    }
}
