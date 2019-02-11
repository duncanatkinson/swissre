package swissre;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleDataStoreTest {

    // Static data
    private LocalDateTime YESTERDAY = LocalDateTime.parse("2018-01-01T13:59:00");
    private LocalDateTime TODAY = LocalDateTime.parse("2018-01-02T13:59:00");

    private DataStore datastore;

    @BeforeEach
    void setUp() {
        this.datastore = new SimpleDataStore();
    }

    @Test
    void getExchangeRatesShouldReturnEmpty() {
        assertEquals(Collections.emptySet(), datastore.getExchangeRateChanges());
    }

    @Test
    void recordShouldAddAnExchangeRateChange() {
        ExchangeRateChange vesExchangeRateChange = new ExchangeRateChange("VES", LocalDateTime.now(), 0.000203941);
        datastore.record(vesExchangeRateChange);
        assertTrue(datastore.getExchangeRateChanges().contains(vesExchangeRateChange));
    }

    @Test
    void recordShouldIgnoreDuplicateExchangeRateChanges() {
        datastore.record(new ExchangeRateChange("VES", TODAY, 0.000203941));
        datastore.record(new ExchangeRateChange("VES", TODAY, 0.000203941));
        assertEquals(1, datastore.getExchangeRateChanges().size());
    }

    @Test
    void recordShouldFlagDayOnDayChangesOf20PercentOrMore() {
        ExchangeRateChange yesterdaysChange = new ExchangeRateChange("GBP", YESTERDAY, 1.0);
        ExchangeRateChange todaysChange = new ExchangeRateChange("GBP", TODAY, 1.2);
        datastore.record(yesterdaysChange);
        datastore.record(todaysChange);
        List<FlaggedChange> flaggedChanges = datastore.getFlaggedChanges();

        assertEquals(1, flaggedChanges.size());
        assertEquals(20, flaggedChanges.get(0).getPercentageChange());
        assertEquals(yesterdaysChange, flaggedChanges.get(0).getOlderRateChange());
        assertEquals(todaysChange, flaggedChanges.get(0).getNewerRateChange());
    }

    @Test
    void recordShouldNotFlagDayOnDayChangesOf20PercentOrMoreForDifferentCurrencies() {
        datastore.record(new ExchangeRateChange("GBP", YESTERDAY, 1.0));
        datastore.record(new ExchangeRateChange("CAD", TODAY, 1.2));
        List<FlaggedChange> flaggedChanges = datastore.getFlaggedChanges();

        assertEquals(0, flaggedChanges.size());
    }

    @Test
    void recordShouldNotFlagDayOnDayChangesOfLessThan20Percent() {
        datastore.record(new ExchangeRateChange("GBP", YESTERDAY, 1.0));
        datastore.record(new ExchangeRateChange("GBP", TODAY, 1.1999999999));
        List<FlaggedChange> flaggedChanges = datastore.getFlaggedChanges();

        assertEquals(0, flaggedChanges.size());
    }

    @Test
    void recordShouldFlagDayOnDayChangesOf30PercentOrMoreRegardlessOfOrdering() {
        ExchangeRateChange todaysChange = new ExchangeRateChange("GBP", TODAY, 1.3);
        ExchangeRateChange yesterdaysChange = new ExchangeRateChange("GBP", YESTERDAY, 1.0);
        datastore.record(todaysChange);
        datastore.record(yesterdaysChange);
        List<FlaggedChange> flaggedChanges = datastore.getFlaggedChanges();

        assertEquals(1, flaggedChanges.size());
        assertEquals(30, flaggedChanges.get(0).getPercentageChange());
        assertEquals(yesterdaysChange, flaggedChanges.get(0).getOlderRateChange());
        assertEquals(todaysChange, flaggedChanges.get(0).getNewerRateChange());
    }

    @Test
    void recordShouldFlagDayOnDayChangesOfFractionalPercent() {
        ExchangeRateChange yesterdaysChange = new ExchangeRateChange("GBP", YESTERDAY, 1.0);
        ExchangeRateChange todaysChange = new ExchangeRateChange("GBP", TODAY, 1.599);
        datastore.record(yesterdaysChange);
        datastore.record(todaysChange);
        List<FlaggedChange> flaggedChanges = datastore.getFlaggedChanges();

        assertEquals(59.9, flaggedChanges.get(0).getPercentageChange());
    }

    @Test
    void recordShouldFlagDayOnDayDecreasesOfMoreThan20Percent() {
        ExchangeRateChange yesterdaysChange = new ExchangeRateChange("GBP", YESTERDAY, 1.201);
        ExchangeRateChange todaysChange = new ExchangeRateChange("GBP", TODAY, 1.0);
        datastore.record(yesterdaysChange);
        datastore.record(todaysChange);
        List<FlaggedChange> flaggedChanges = datastore.getFlaggedChanges();

        assertEquals(20.1, flaggedChanges.get(0).getPercentageChange());
    }

    @Test
    void recordShouldFlagExchangeRateChangesGivenInsertedInMiddleOfTwoExistingRateChanges() {
        ExchangeRateChange dayBeforeYesterdaysChange = new ExchangeRateChange("GBP", YESTERDAY.minusDays(1), 1.0);
        ExchangeRateChange yesterdaysChange = new ExchangeRateChange("GBP", YESTERDAY, 1.2);
        ExchangeRateChange todaysChange = new ExchangeRateChange("GBP", TODAY, 1.0);
        datastore.record(dayBeforeYesterdaysChange);
        datastore.record(todaysChange);

        datastore.record(yesterdaysChange);// inserted in the middle,
        List<FlaggedChange> flaggedChanges = datastore.getFlaggedChanges();

        assertEquals(2, flaggedChanges.size());
        assertEquals(20, flaggedChanges.get(0).getPercentageChange());
        assertEquals(20, flaggedChanges.get(1).getPercentageChange());
    }
}