package swissre.persistence;

import swissre.model.ExchangeRateChange;
import swissre.model.CurrencyCode;
import swissre.model.FlaggedChange;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.groupingBy;

/**
 * Simple in memory implementation of the {@link DataStore}.
 *
 * Any attempt to store an exchange rate change which has already been received will be ignored.
 * @author Duncan Atkinson
 */
public class SimpleDataStore implements DataStore {

    private final Map<CurrencyCode, List<ExchangeRateChange>> exchangeRateChanges;

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
        CurrencyCode currencyCode = exchangeRateChange.getCurrencyCode();
        if (!this.exchangeRateChanges.containsKey(currencyCode)) {
            this.exchangeRateChanges.put(currencyCode, new LinkedList<>());
        }
        List<ExchangeRateChange> changesForCurrency = this.exchangeRateChanges.get(currencyCode);
        Optional<ExchangeRateChange> previousChange = changesForCurrency.stream()
                .filter(changesBefore(exchangeRateChange.getTimestamp()))
                .max(comparingByTimestamp());

        Optional<ExchangeRateChange> nextChange = changesForCurrency.stream()
                .filter(changesAfter(exchangeRateChange.getTimestamp()))
                .min(comparingByTimestamp());

        // compare with before and after just in case file loads are out of order
        previousChange.ifPresent((prev) -> flagIfDramaticRateChange(exchangeRateChange, prev));
        nextChange.ifPresent((next) -> flagIfDramaticRateChange(exchangeRateChange, next));
        this.exchangeRateChanges.get(currencyCode).add(exchangeRateChange);
    }

    private void flagIfDramaticRateChange(ExchangeRateChange exchangeRateChange, ExchangeRateChange nextChange) {
        double percentageRateChange = getPercentageRateChange(exchangeRateChange, nextChange);
        if (percentageRateChange >= 20) {
            flaggedChanges.add(new FlaggedChange(percentageRateChange, exchangeRateChange, nextChange));
        }
    }

    private double getPercentageRateChange(ExchangeRateChange exchangeRateChange, ExchangeRateChange previousChange) {
        BigDecimal smallerRate;
        BigDecimal biggerRate;
        if (previousChange.getRateAgainstUSD() < exchangeRateChange.getRateAgainstUSD()) {
            smallerRate = BigDecimal.valueOf(previousChange.getRateAgainstUSD());
            biggerRate = BigDecimal.valueOf(exchangeRateChange.getRateAgainstUSD());
        }else{
            biggerRate = BigDecimal.valueOf(previousChange.getRateAgainstUSD());
            smallerRate = BigDecimal.valueOf(exchangeRateChange.getRateAgainstUSD());
        }

        BigDecimal absoluteChange = biggerRate.subtract(smallerRate).abs();
        BigDecimal divide = absoluteChange.divide(smallerRate, ROUND_HALF_UP);
        BigDecimal percentageChange = divide.multiply(BigDecimal.valueOf(100.0));
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

    private Predicate<ExchangeRateChange> changesAfter(LocalDateTime time) {
        return (change) -> change.getTimestamp().isAfter(time);
    }

    @Override
    public List<FlaggedChange> getFlaggedChanges() {
        return flaggedChanges;
    }

    @Override
    public Map<String, Double> getAveragesByMonth(CurrencyCode currencyCode) {
        return exchangeRateChanges.get(currencyCode).stream()
                .collect(groupingBy(yearAndMonth(), averagingDouble(ExchangeRateChange::getRateAgainstUSD)));
    }

    @Override
    public Map<Integer, Double> getAveragesByYear(CurrencyCode currencyCode) {
        return exchangeRateChanges.get(currencyCode).stream()
                .collect(groupingBy(year(), averagingDouble(ExchangeRateChange::getRateAgainstUSD)));
    }

    private Function<ExchangeRateChange, String> yearAndMonth() {
        return exchangeRateChange -> {
            return exchangeRateChange.getTimestamp().getYear() + "_" + exchangeRateChange.getTimestamp().getMonth();
        };
    }

    private Function<ExchangeRateChange, Integer> year() {
        return exchangeRateChange -> exchangeRateChange.getTimestamp().getYear();
    }
}
