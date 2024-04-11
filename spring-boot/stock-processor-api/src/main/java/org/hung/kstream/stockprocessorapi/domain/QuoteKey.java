package org.hung.kstream.stockprocessorapi.domain;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class QuoteKey {
    private String market;
    private String ticker;

    @JsonFormat(pattern ="yyyy-MM-dd")
    private LocalDate tradeDate;
}
