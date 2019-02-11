package swissre;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyCodeTest {

    @Test
    void shouldConstruct() {
        CurrencyCode.valueOf("USD");
    }

    @Test
    void shouldBeEqual() {
        assertEquals(CurrencyCode.valueOf("EUR"), CurrencyCode.valueOf("EUR"));
    }

    @Test
    void shouldNotBeEqual() {
        assertNotEquals(CurrencyCode.valueOf("EUR"), CurrencyCode.valueOf("USD"));
    }

    @Test
    void shouldHaveSameHashcode() {
        assertEquals(CurrencyCode.valueOf("EUR").hashCode(), CurrencyCode.valueOf("EUR").hashCode());
    }
}