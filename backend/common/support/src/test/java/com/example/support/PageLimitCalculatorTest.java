package com.example.support;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PageLimitCalculatorTest {

    @ParameterizedTest
    @CsvSource({
           "1, 10, 10, 101",
           "7, 30, 10, 301",
           "11, 30, 10, 601",
           "20, 30, 10, 601",
           "21, 30, 10, 901",
    })
    void pageLimitTest(Long page, Long pageSize, Long movablePageCount, Long expected) {
        calculatePageLimitBy(page, pageSize, movablePageCount, expected);
    }

    void calculatePageLimitBy(Long page, Long pageSize, Long movablePageCount, Long expected) {
        Long result = PageLimitCalculator.pageLimit(page, pageSize, movablePageCount);
        assertThat(result).isEqualTo(expected);
    }

}
