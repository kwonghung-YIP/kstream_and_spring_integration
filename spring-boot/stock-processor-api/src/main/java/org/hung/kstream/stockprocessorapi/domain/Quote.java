package org.hung.kstream.stockprocessorapi.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

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

    @JsonFormat(shape=Shape.STRING)//pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ",timezone=JsonFormat.DEFAULT_TIMEZONE)
    private Instant lastUpdDate;
}
