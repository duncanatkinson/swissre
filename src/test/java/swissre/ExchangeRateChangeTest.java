package swissre;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class ExchangeRateChangeTest {

    @Test
    void shouldConstruct() {
        new ExchangeRateChange("CHF", LocalDateTime.now(), 0.776463);
    }

    @Test
    void shouldFailToConstructGivenInvalidCurrency() {
        new ExchangeRateChange("MONOPOLY", LocalDateTime.now(), 0.776463);
    }
}