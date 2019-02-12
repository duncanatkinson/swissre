package swissre.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class ExchangeRateChange {

    private final CurrencyCode currencyCode;
    private final LocalDateTime timestamp; //TODO determine if this should be zonedDateTime
    private final Double rateAgainstUSD;

    public ExchangeRateChange(String currency, LocalDateTime timestamp, Double rateAgainstUSD) {
        this(CurrencyCode.valueOf(currency), timestamp, rateAgainstUSD);
    }

    public ExchangeRateChange(CurrencyCode currencyCode, LocalDateTime timestamp, Double rateAgainstUSD) {
        this.currencyCode = currencyCode;
        this.timestamp = timestamp;
        this.rateAgainstUSD = rateAgainstUSD;
    }

    public CurrencyCode getCurrencyCode() {
        return currencyCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Double getRateAgainstUSD() {
        return rateAgainstUSD;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeRateChange that = (ExchangeRateChange) o;
        return Objects.equals(currencyCode, that.currencyCode) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(rateAgainstUSD, that.rateAgainstUSD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currencyCode, timestamp, rateAgainstUSD);
    }

    @Override
    public String toString() {
        return "ExchangeRateChange{" +
                "currencyCode=" + currencyCode +
                ", timestamp=" + timestamp +
                ", rateAgainstUSD=" + rateAgainstUSD +
                '}';
    }
}
