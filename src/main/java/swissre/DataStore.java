package swissre;

import java.time.LocalDate;
import java.util.Set;

/**
 * Data store for the purposes of this test.
 */
public interface DataStore {

    Set<ExchangeRateChange> getExchangeRates();

    /**
     * Record an exchange rate change along with the file date.
     * @param exchangeRateChange
     * @param fileDate
     */
    void record(ExchangeRateChange exchangeRateChange, LocalDate fileDate);
}
