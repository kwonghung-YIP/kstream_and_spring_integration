package org.hung.kstream.stockkstream.domain;

import java.time.LocalDate;

import lombok.Data;

@Data
public class QuoteKey {
    
    private String market;
    private String ticker;
    private LocalDate tradeDate;
}
