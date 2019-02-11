package swissre;

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
}
