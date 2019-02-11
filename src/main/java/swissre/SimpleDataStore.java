package swissre;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ROUND_HALF_UP;

/**
 * Simple in memory implementation of the {@link DataStore}.
 * <p>
 * Any attempt to store an exchange rate change which has already been received will be ignored.
 */
public class SimpleDataStore implements DataStore {

    private final Map<Currency, List<ExchangeRateChange>> exchangeRateChanges;

    private final List<FlaggedChange> flaggedChanges;

    public SimpleDataStore() {
        this.exchangeRateChanges = new HashMap<>();
        this.flaggedChanges = new ArrayList<>();
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
        if (!this.exchangeRateChanges.containsKey(currency)) {
            this.exchangeRateChanges.put(currency, new LinkedList<>());
        }
        List<ExchangeRateChange> changesForCurrency = this.exchangeRateChanges.get(currency);
        Optional<ExchangeRateChange> previousChange = changesForCurrency.stream()
                .filter(changesBefore(exchangeRateChange.getTimestamp()))
                .max(comparingByTimestamp());

        if(previousChange.isPresent()){
            double doubleValue = getPercentageRateChange(exchangeRateChange, previousChange.get());
            if(doubleValue >= 20){
                flaggedChanges.add(new FlaggedChange());
            }
        }
        this.exchangeRateChanges.get(currency).add(exchangeRateChange);
    }

    private double getPercentageRateChange(ExchangeRateChange exchangeRateChange, ExchangeRateChange previousChange) {
        BigDecimal previousRate = BigDecimal.valueOf(previousChange.getRateAgainstUSD());
        BigDecimal newRate = BigDecimal.valueOf(exchangeRateChange.getRateAgainstUSD());

        BigDecimal change = previousRate.subtract(newRate).abs();

        BigDecimal percentageChange = change.divide(newRate, ROUND_HALF_UP).multiply(BigDecimal.valueOf(100.0));

        return percentageChange.doubleValue();
    }

    private Comparator<ExchangeRateChange> comparingByTimestamp() {
        return (a, b) -> {
            if (a.equals(b)) {
                return 0;
            } else {
                return a.getTimestamp().isBefore(b.getTimestamp()) ? -1 : 1;
            }
        };
    }

    private Predicate<ExchangeRateChange> changesBefore(LocalDateTime time) {
        return (change) -> change.getTimestamp().isBefore(time);
    }

    @Override
    public List<FlaggedChange> getFlaggedChanges() {
        return flaggedChanges;
    }
}
