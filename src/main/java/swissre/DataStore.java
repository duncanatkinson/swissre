package swissre;

import java.util.List;
import java.util.Set;

/**
 * Data store for the purposes of this test.
 *
 */
public interface DataStore {

    /**
     * @return all exchange rates
     */
    Set<ExchangeRateChange> getExchangeRateChanges();

    /**
     * Record an exchange rate change along with the file date.
     *
     * @param exchangeRateChange to record
     */
    void record(ExchangeRateChange exchangeRateChange);

    List<FlaggedChange> getFlaggedChanges();
}
