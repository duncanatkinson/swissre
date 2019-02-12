package swissre.parser;

/**
 * An enum of all expected tokens in the Exchange Rate File
 */
public enum LineMarker {


    START_OF_FILE("START-OF-FILE"),
    START_OF_FIELD_LIST("START-OF-FIELD-LIST"),
    END_OF_FIELD_LIST("END-OF-FIELD-LIST"),
    START_OF_EXCHANGE_RATES("START-OF-EXCHANGE-RATES"),
    END_OF_EXCHANGE_RATES("END-OF-EXCHANGE-RATES"),
    END_OF_FILE("END-OF-FILE"),
    CURRENCY("CURRENCY"),
    EXCHANGE_RATE("EXCHANGE_RATE"),
    LAST_UPDATE("LAST_UPDATE"),
    ;

    private final String stringValue;

    LineMarker(String stringValue) {
        this.stringValue = stringValue;
    }

    public String asString() {
        return stringValue;
    }

    @Override
    public String toString() {
        return stringValue;
    }}
