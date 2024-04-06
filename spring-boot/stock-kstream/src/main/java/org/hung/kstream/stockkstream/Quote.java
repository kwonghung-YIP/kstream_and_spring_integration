package org.hung.kstream.stockkstream;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Quote {
    private UUID id;
    private String ticker;
    private BigDecimal price;
    private long volume;
}
