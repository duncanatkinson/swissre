package swissre;

import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Just a quick and dirty little class to strap the code to a command line of some sort.
 */
public class App {

    private final DataStore dataStore;
    private final Parser<String> parser;

    public App() {
        dataStore = new SimpleDataStore();
        parser = new StringParser(dataStore);
    }

    private void run() {
        while(true) {
            Scanner scanner = new Scanner(System.in);
            ((StringParser) parser).receive(scanner);
            List<FlaggedChange> flagged = dataStore.getFlaggedChanges();
            System.out.println("flagged = " + flagged);
            Set<CurrencyCode> currencyCodes = dataStore.getExchangeRateChanges().stream()
                    .map(ExchangeRateChange::getCurrencyCode)
                    .collect(Collectors.toSet());

            currencyCodes.forEach(code -> {
                System.out.println(code + " monthly averages = " + dataStore.getAveragesByMonth(code));
                System.out.println(code + " yearly averages = " + dataStore.getAveragesByYear(code));
            });
        }
    }

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }
}
