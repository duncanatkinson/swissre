package swissre;

import java.time.LocalDateTime;
import java.util.Objects;

public class ExchangeRateChange {

    private final Currency currency;
    private final LocalDateTime timestamp; //TODO determine if this should be zonedDateTime
    private final Double rateAgainstUSD;

    public ExchangeRateChange(String currency, LocalDateTime timestamp, Double rateAgainstUSD) {
        this.currency = Currency.valueOf(currency);
        this.timestamp = timestamp;
        this.rateAgainstUSD = rateAgainstUSD;
    }

    public Currency getCurrency() {
        return currency;
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
        return Objects.equals(currency, that.currency) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(rateAgainstUSD, that.rateAgainstUSD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, timestamp, rateAgainstUSD);
    }

    @Override
    public String toString() {
        return "ExchangeRateChange{" +
                "currency=" + currency +
                ", timestamp=" + timestamp +
                ", rateAgainstUSD=" + rateAgainstUSD +
                '}';
    }
}
