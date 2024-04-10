package org.hung.kstream.stockkstream.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class Quote {
    private String market;
    private String ticker;
    
    @JsonFormat(pattern ="yyyy-MM-dd")
    private LocalDate tradeDate;
    
    private BigDecimal spot;
    private BigDecimal open;
    private BigDecimal low;
    private BigDecimal high;
    private BigDecimal close;
    private BigDecimal change;
    private BigInteger volume;
    private long ver;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastUpdDate;
}
