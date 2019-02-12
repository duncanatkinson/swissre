package swissre;

import java.util.Objects;

/**
 * An exchange rate change which needs to be flagged against another exchange rate change.
 */
public class FlaggedChange {

    private final ExchangeRateChange olderRateChange;
    private final ExchangeRateChange newerRateChange;
    private final double percentageRateChange;

    public FlaggedChange(double percentageRateChange, ExchangeRateChange a, ExchangeRateChange b) {
        if (a.getTimestamp().isAfter(b.getTimestamp())) {
            this.olderRateChange = b;
            this.newerRateChange = a;
        } else {
            this.olderRateChange = a;
            this.newerRateChange = b;
        }
        this.percentageRateChange = percentageRateChange;
    }

    public double getPercentageChange() {
        return percentageRateChange;
    }

    public ExchangeRateChange getOlderRateChange() {
        return olderRateChange;
    }

    public ExchangeRateChange getNewerRateChange() {
        return newerRateChange;
    }

    @Override
    public String toString() {
        return "FlaggedChange{" +
                "olderRateChange=" + olderRateChange +
                ", newerRateChange=" + newerRateChange +
                ", percentageRateChange=" + percentageRateChange +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlaggedChange that = (FlaggedChange) o;
        return Double.compare(that.percentageRateChange, percentageRateChange) == 0 &&
                Objects.equals(olderRateChange, that.olderRateChange) &&
                Objects.equals(newerRateChange, that.newerRateChange);
    }

    @Override
    public int hashCode() {
        return Objects.hash(olderRateChange, newerRateChange, percentageRateChange);
    }
}
