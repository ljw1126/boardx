package com.example.hotarticle.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class TimeCalculatorTest {

    @DisplayName("현재 시간에서 자정까지 남은 시간을 계산한다")
    @Test
    void calculateDurationToMidnight() {
        LocalDateTime now = LocalDateTime.of(2025, 7, 11, 12, 0, 0);
        Duration duration = TimeCalculator.calculateDurationToMidnight(now);

        assertThat(duration).isEqualTo(Duration.ofHours(12));

        log.info("duration.getSeconds() / 60 = {}", duration.getSeconds() / 60);
    }
}
