package com.viratech.dto;

import java.math.BigDecimal;

public record ExchangeRequest(
        Long id,
        String from,
        String to,
        BigDecimal conversionFactor,
        BigDecimal convertedValue,
        String environment
) {
}
