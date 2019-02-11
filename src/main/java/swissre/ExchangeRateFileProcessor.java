package swissre;

/**
 * Exchange rate file processor, should process files as defined below
 *
 * START-OF-FILE
 * DATE=20181015
 * START-OF-FIELD-LIST
 * CURRENCY
 * EXCHANGE_RATE
 * LAST_UPDATE
 * END-OF-FIELD-LIST
 * START-OF-EXCHANGE-RATES
 * CHF|0.9832|17:12:59 10/14/2018|
 * GBP|0.7849|17:12:59 10/14/2018|
 * EUR|0.8677|17:13:00 10/14/2018|
 * END-OF-EXCHANGE-RATES
 * END-OF-FILE
 *
 * The EXCHANGE_RATE is always against the US Dollar e.g. 1 USD = 0.7846 GBP.
 * @param <T> being the type of the file to process
 */
public interface ExchangeRateFileProcessor <T> {

    /**
     * @param file is a state of the art exchange rate file.
     * @throws InvalidExchangeRateFileException
     */
    void receiveFile(T file) throws InvalidExchangeRateFileException;
}
