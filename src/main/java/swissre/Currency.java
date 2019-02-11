package swissre;

import java.util.Objects;

/**
 * Represents a currency.
 */
public class Currency {

    private final String val;

    private Currency(String val) {
        this.val = val;
    }

    public static Currency valueOf(String currency) {
        return new Currency(currency);
    }

    public String getVal() {
        return val;
    }

    @Override
    public String toString() {
        return val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Currency currency = (Currency) o;
        return Objects.equals(val, currency.val);
    }

    @Override
    public int hashCode() {
        return Objects.hash(val);
    }
}
