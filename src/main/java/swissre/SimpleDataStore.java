package swissre;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple in memory implementation of the {@link DataStore}.
 * <p>
 * Any attempt to store an exchange rate change which has already been received will be ignored.
 */
public class SimpleDataStore implements DataStore {

    private final Map<Currency, Set<ExchangeRateChange>> exchangeRateChanges;

    public SimpleDataStore() {
        this.exchangeRateChanges = new HashMap<>();
    }

    @Override
    public Set<ExchangeRateChange> getExchangeRateChanges() {
        return exchangeRateChanges.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public void record(ExchangeRateChange exchangeRateChange) {
        Currency currency = exchangeRateChange.getCurrency();
        if(!this.exchangeRateChanges.containsKey(currency)){
            this.exchangeRateChanges.put(currency,new HashSet<>());
        }
        this.exchangeRateChanges.get(currency).add(exchangeRateChange);
    }

    @Override
    public List<FlaggedChange> getFlaggedChanges() {
        return null;
    }
}
