package org.hung.kstream.stockkstream.domain;

import java.math.BigInteger;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class VolumeTable {
    private QuoteKey key;
    private BigInteger volume;
    private long ver;
    private LocalDateTime lastUpdDate;
}
