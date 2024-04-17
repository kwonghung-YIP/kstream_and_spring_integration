package org.hung.kstream.stockprocessorapi.domain;

import java.time.LocalDate;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QuoteKey {
    private String market;
    private String ticker;

    @JsonFormat(pattern ="yyyy-MM-dd")
    private LocalDate tradeDate;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof QuoteKey other) {
            if (this.market==null || other.market==null) {
                return false;
            }
            if (this.ticker==null || other.ticker==null) {
                return false;
            }
            if (this.tradeDate==null || other.tradeDate==null) {
                return false;
            }
            return this.market.equalsIgnoreCase(other.market) &&
                this.ticker.equalsIgnoreCase(other.ticker) &&
                this.tradeDate.isEqual(other.tradeDate); 
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.market!=null && this.ticker!=null && this.tradeDate!=null) {
            return this.market.hashCode() + this.ticker.hashCode() + this.tradeDate.hashCode();
        }
        return super.hashCode();
    }
}
