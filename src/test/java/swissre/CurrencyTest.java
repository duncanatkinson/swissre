package swissre;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyTest {

    @Test
    void shouldConstruct() {
        Currency.valueOf("USD");
    }

    @Test
    void shouldBeEqual() {
        assertEquals(Currency.valueOf("EUR"), Currency.valueOf("EUR"));
    }

    @Test
    void shouldNotBeEqual() {
        assertNotEquals(Currency.valueOf("EUR"), Currency.valueOf("USD"));
    }

    @Test
    void shouldHaveSameHashcode() {
        assertEquals(Currency.valueOf("EUR").hashCode(), Currency.valueOf("EUR").hashCode());
    }
}