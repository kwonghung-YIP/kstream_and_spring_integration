package org.hung.kstream.stockkstream.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Quote {
    private String market;
    private String ticker;
    private LocalDate tradeDate;
    private BigDecimal spot;
    private BigDecimal open;
    private BigDecimal low;
    private BigDecimal high;
    private BigDecimal close;
    private BigDecimal change;
    private BigInteger volume;
    private long ver;
    private LocalDateTime lastUpdDate;
}
