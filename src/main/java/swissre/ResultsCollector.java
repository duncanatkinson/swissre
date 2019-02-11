package swissre;

import java.util.Set;

public interface ResultsCollector {

    Set<ExchangeRateChange> getExchangeRates();

    void record(ExchangeRateChange exchangeRateChange);
}
