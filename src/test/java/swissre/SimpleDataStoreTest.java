package swissre;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SimpleDataStoreTest {

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
        LocalDateTime timestamp = LocalDateTime.now();
        datastore.record(new ExchangeRateChange("VES", timestamp, 0.000203941));
        datastore.record(new ExchangeRateChange("VES", timestamp, 0.000203941));
        assertEquals(1, datastore.getExchangeRateChanges().size());
    }

    @Test
    void recordShouldFlagDayOnDayChangesOf20PercentOrMore() {
        LocalDateTime yesterday = LocalDateTime.parse("2018-01-01T13:59:00");
        LocalDateTime today = LocalDateTime.parse("2018-01-02T13:59:00");
        datastore.record(new ExchangeRateChange("GBP", yesterday, 1.0));
        datastore.record(new ExchangeRateChange("GBP", today, 1.2));
        List<FlaggedChange> flaggedChanges =  datastore.getFlaggedChanges();

        assertEquals(1, flaggedChanges.size());
        assertEquals(20, flaggedChanges.get(0).getPercentageChange());
    }
}