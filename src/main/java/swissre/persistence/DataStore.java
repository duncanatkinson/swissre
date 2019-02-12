package swissre.persistence;

import swissre.model.ExchangeRateChange;
import swissre.model.CurrencyCode;
import swissre.model.FlaggedChange;

import java.util.List;
import java.util.Map;
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

    /**
     * @return a list of rate changes which have been flagged
     */
    List<FlaggedChange> getFlaggedChanges();

    /**
     * @param currencyCode to retrieve averages for
     * @return a Map where the key is a String in the format 2018_JANUARY
     */
    Map<String,Double> getAveragesByMonth(CurrencyCode currencyCode);

    /**
     * @param currencyCode to retrieve averages for
     * @return a Map where the key is the year as an Integer
     */
    Map<Integer, Double> getAveragesByYear(CurrencyCode currencyCode);
}
