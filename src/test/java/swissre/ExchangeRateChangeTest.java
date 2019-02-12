package swissre;

import org.junit.jupiter.api.Test;
import swissre.model.ExchangeRateChange;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Would add test for the equals and hashcode methods using a specific library here.
 */
class ExchangeRateChangeTest {

    @Test
    void shouldConstruct() {
        new ExchangeRateChange("CHF", LocalDateTime.now(), 0.776463);
    }

    @Test
    void shouldBeEqual() {
        LocalDateTime now = LocalDateTime.now();
        ExchangeRateChange a = new ExchangeRateChange("CHF", now, 0.776463);
        ExchangeRateChange b = new ExchangeRateChange("CHF", now, 0.776463);
        assertEquals(a, b);
    }

    @Test
    void shouldNotBeEqualGivenDifferentCurrency() {
        LocalDateTime now = LocalDateTime.now();
        ExchangeRateChange a = new ExchangeRateChange("CHF", now, 0.776463);
        ExchangeRateChange b = new ExchangeRateChange("GBP", now, 0.776463);
        assertNotEquals(a, b);
    }

    @Test
    void shouldNotBeEqualGivenDifferentTimestamp() {
        ExchangeRateChange a = new ExchangeRateChange("CHF", LocalDateTime.now(), 0.776463);
        ExchangeRateChange b = new ExchangeRateChange("CHF", LocalDateTime.now().plusSeconds(1), 0.776463);
        assertNotEquals(a, b);
    }

    @Test
    void shouldNotBeEqualGivenDifferentExchangeRate() {
        ExchangeRateChange a = new ExchangeRateChange("CHF", LocalDateTime.now(), 0.776463);
        ExchangeRateChange b = new ExchangeRateChange("CHF", LocalDateTime.now(), 0.7764631);
        assertNotEquals(a, b);
    }

    @Test
    void shouldBeSameHashcode() {
        LocalDateTime now = LocalDateTime.now();
        ExchangeRateChange a = new ExchangeRateChange("CHF", now, 0.776463);
        ExchangeRateChange b = new ExchangeRateChange("CHF", now, 0.776463);
        assertEquals(a.hashCode(), b.hashCode());
    }
}