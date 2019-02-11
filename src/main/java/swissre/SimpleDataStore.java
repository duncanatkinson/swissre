package swissre;

import java.time.LocalDate;
import java.util.Set;

/**
 * Simple in memory implementation of the {@link DataStore}.
 *
 * Any attempt to store an exchange rate change which has already been received will be ignored.
 *
 */
public class SimpleDataStore implements DataStore {

    @Override
    public Set<ExchangeRateChange> getExchangeRates() {
        return null;
    }

    @Override
    public void record(ExchangeRateChange exchangeRateChange, LocalDate fileDate) {

    }
}
