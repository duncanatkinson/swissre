package swissre;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import swissre.model.CurrencyCode;
import swissre.model.ExchangeRateChange;
import swissre.model.FlaggedChange;
import swissre.persistence.DataStore;
import swissre.persistence.SimpleDataStore;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleDataStoreTest {

    //for the purpose of the test let's say today is the second of jan.
    private static final LocalDateTime YESTERDAY = LocalDateTime.parse("2018-01-01T13:59:00");
    private static final LocalDateTime TODAY = LocalDateTime.parse("2018-01-02T13:59:00");
    public static final CurrencyCode GBP = CurrencyCode.valueOf("GBP");
    public static final CurrencyCode CAD = CurrencyCode.valueOf("CAD");
    public static final CurrencyCode VES = CurrencyCode.valueOf("VES");

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
        ExchangeRateChange vesExchangeRateChange = new ExchangeRateChange(VES, LocalDateTime.now(), 0.000203941);
        datastore.record(vesExchangeRateChange);
        assertTrue(datastore.getExchangeRateChanges().contains(vesExchangeRateChange));
    }

    @Test
    void recordShouldIgnoreDuplicateExchangeRateChanges() {
        datastore.record(new ExchangeRateChange(VES, TODAY, 0.000203941));
        datastore.record(new ExchangeRateChange(VES, TODAY, 0.000203941));
        assertEquals(1, datastore.getExchangeRateChanges().size());
    }

    @Test
    void recordShouldFlagDayOnDayChangesOf20PercentOrMore() {
        ExchangeRateChange yesterdaysChange = new ExchangeRateChange(GBP, YESTERDAY, 1.0);
        ExchangeRateChange todaysChange = new ExchangeRateChange(GBP, TODAY, 1.2);
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
        datastore.record(new ExchangeRateChange(GBP, YESTERDAY, 1.0));
        datastore.record(new ExchangeRateChange(CAD, TODAY, 1.2));
        List<FlaggedChange> flaggedChanges = datastore.getFlaggedChanges();

        assertEquals(0, flaggedChanges.size());
    }

    @Test
    void recordShouldNotFlagDayOnDayChangesOfLessThan20Percent() {
        datastore.record(new ExchangeRateChange(GBP, YESTERDAY, 1.0));
        datastore.record(new ExchangeRateChange(GBP, TODAY, 1.1999999999));
        List<FlaggedChange> flaggedChanges = datastore.getFlaggedChanges();

        assertEquals(0, flaggedChanges.size());
    }

    @Test
    void recordShouldFlagDayOnDayChangesOf30PercentOrMoreRegardlessOfOrdering() {
        ExchangeRateChange todaysChange = new ExchangeRateChange(GBP, TODAY, 1.3);
        ExchangeRateChange yesterdaysChange = new ExchangeRateChange(GBP, YESTERDAY, 1.0);
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
        ExchangeRateChange yesterdaysChange = new ExchangeRateChange(GBP, YESTERDAY, 1.0);
        ExchangeRateChange todaysChange = new ExchangeRateChange(GBP, TODAY, 1.599);
        datastore.record(yesterdaysChange);
        datastore.record(todaysChange);
        List<FlaggedChange> flaggedChanges = datastore.getFlaggedChanges();

        assertEquals(59.9, flaggedChanges.get(0).getPercentageChange());
    }

    @Test
    void recordShouldFlagDayOnDayDecreasesOfMoreThan20Percent() {
        ExchangeRateChange yesterdaysChange = new ExchangeRateChange(GBP, YESTERDAY, 1.201);
        ExchangeRateChange todaysChange = new ExchangeRateChange(GBP, TODAY, 1.0);
        datastore.record(yesterdaysChange);
        datastore.record(todaysChange);
        List<FlaggedChange> flaggedChanges = datastore.getFlaggedChanges();

        assertEquals(20.1, flaggedChanges.get(0).getPercentageChange());
    }

    @Test
    void recordShouldFlagExchangeRateChangesGivenInsertedInMiddleOfTwoExistingRateChanges() {
        ExchangeRateChange dayBeforeYesterdaysChange = new ExchangeRateChange(GBP, YESTERDAY.minusDays(1), 1.0);
        ExchangeRateChange yesterdaysChange = new ExchangeRateChange(GBP, YESTERDAY, 1.2);
        ExchangeRateChange todaysChange = new ExchangeRateChange(GBP, TODAY, 1.0);
        datastore.record(dayBeforeYesterdaysChange);
        datastore.record(todaysChange);

        datastore.record(yesterdaysChange);// inserted in the middle triggering two large day on day rate changes.
        List<FlaggedChange> flaggedChanges = datastore.getFlaggedChanges();

        assertEquals(2, flaggedChanges.size());
        assertEquals(20, flaggedChanges.get(0).getPercentageChange());
        assertEquals(20, flaggedChanges.get(1).getPercentageChange());
    }

    /**
     * These could be complicated depending on the definition of average. for the purpose of the 'simple' test
     * I will make it an average of the recorded entries. Obviously if a rate was 1.0 with a single recorded change
     * at the start of the month and then at the end of the month we had lots of of records of 0.01 then the average
     * would be close to 0.01 despite the rate being 1.0 for the entire month.
     */
    @Test
    void shouldCalculateAverageRatesByMonthForCurrency() {
        //Jan
        datastore.record(new ExchangeRateChange(GBP, LocalDateTime.parse("2018-01-01T13:59:00"), 0.5));
        datastore.record(new ExchangeRateChange(GBP, LocalDateTime.parse("2018-01-02T13:59:00"), 1.0));
        datastore.record(new ExchangeRateChange(GBP, LocalDateTime.parse("2018-01-03T13:59:00"), 1.5));
        datastore.record(new ExchangeRateChange(CAD, LocalDateTime.parse("2018-02-02T13:59:00"), 2.0));

        // Feb
        datastore.record(new ExchangeRateChange(GBP, LocalDateTime.parse("2018-02-02T13:59:00"), 2.0));
        datastore.record(new ExchangeRateChange(CAD, LocalDateTime.parse("2018-02-02T13:59:00"), 3.0));
        Map<String, Double> gbpMonthlyAverages = datastore.getAveragesByMonth(GBP);

        assertEquals(1.0, gbpMonthlyAverages.get("2018_JANUARY").doubleValue());
        assertEquals(2.0, gbpMonthlyAverages.get("2018_FEBRUARY").doubleValue());
    }

    /**
     * These could be complicated depending on the definition of average. for the purpose of the 'simple' test
     * I will make it an average of the recorded entries. Obviously if a rate was 1.0 with a single recorded change
     * at the start of the year and then at the end of the year we had lots of of records of 0.01 then the average
     * would be close to 0.01 despite the rate being 1.0 for the entire year.
     */
    @Test
    void shouldCalculateAverageRatesByYearForCurrency() {
        //2018
        datastore.record(new ExchangeRateChange(GBP, LocalDateTime.parse("2018-01-01T13:59:00"), 0.5));
        datastore.record(new ExchangeRateChange(GBP, LocalDateTime.parse("2018-02-01T13:59:00"), 1.0));
        datastore.record(new ExchangeRateChange(GBP, LocalDateTime.parse("2018-03-01T13:59:00"), 1.5));
        datastore.record(new ExchangeRateChange(CAD, LocalDateTime.parse("2018-04-01T13:59:00"), 2.0));

        // 2019
        datastore.record(new ExchangeRateChange(GBP, LocalDateTime.parse("2019-02-01T13:59:00"), 2.0));
        datastore.record(new ExchangeRateChange(CAD, LocalDateTime.parse("2019-02-01T13:59:00"), 3.0));
        Map<Integer, Double> gbpYearlyAverages = datastore.getAveragesByYear(GBP);

        assertEquals(1.0, gbpYearlyAverages.get(2018).doubleValue());
        assertEquals(2.0, gbpYearlyAverages.get(2019).doubleValue());
    }
}