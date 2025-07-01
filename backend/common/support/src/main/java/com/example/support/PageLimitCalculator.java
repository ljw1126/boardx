package com.example.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageLimitCalculator {
    public static Long pageLimit(Long page, Long pageSize, Long movablePageCount) {
        return (((page - 1) / movablePageCount) + 1) * pageSize * movablePageCount + 1;
    }
}
