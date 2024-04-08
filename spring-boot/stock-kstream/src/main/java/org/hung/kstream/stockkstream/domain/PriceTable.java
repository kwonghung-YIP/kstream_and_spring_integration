package org.hung.kstream.stockkstream.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PriceTable {
    private QuoteKey key;
    private BigDecimal spot;
    private BigDecimal open;
    private BigDecimal low;
    private BigDecimal high;
    private BigDecimal close;
    private BigDecimal change;
    private long ver;
    private LocalDateTime lastUpdDate;
}
