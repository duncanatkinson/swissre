package swissre.model;

import java.util.Objects;

/**
 * Represents an ISO 4217 currency code.
 */
public class CurrencyCode {

    private final String val;

    private CurrencyCode(String val) {
        this.val = val;
    }

    public static CurrencyCode valueOf(String currency) {
        return new CurrencyCode(currency);
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
        CurrencyCode currencyCode = (CurrencyCode) o;
        return Objects.equals(val, currencyCode.val);
    }

    @Override
    public int hashCode() {
        return Objects.hash(val);
    }
}
