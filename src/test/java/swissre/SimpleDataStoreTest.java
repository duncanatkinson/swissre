package swissre;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SimpleDataStoreTest {

    private DataStore datastore;

    @BeforeEach
    void setUp() {
        this.datastore = new SimpleDataStore();
    }

    @Test
    void getExchangeRatesShouldReturnEmpty() {
        assertEquals(Collections.emptySet(), datastore.getExchangeRates());
    }
}