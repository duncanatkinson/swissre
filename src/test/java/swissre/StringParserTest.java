package swissre;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import swissre.model.CurrencyCode;
import swissre.model.ExchangeRateChange;
import swissre.model.FlaggedChange;
import swissre.parser.InvalidExchangeRateFileException;
import swissre.parser.Parser;
import swissre.parser.StringParser;
import swissre.persistence.DataStore;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StringParserTest {

    private Parser<String> stringParser;

    private DataStore dataStoreStub;

    @BeforeEach
    void setUp() {
        // simple stub just to record the data passed to the store, obviously would usually be mocked.
        this.dataStoreStub = new DataStore() {

            Set<ExchangeRateChange> recordedExchangeRates = new HashSet<>();

            @Override
            public Set<ExchangeRateChange> getExchangeRateChanges() {
                return recordedExchangeRates;
            }

            @Override
            public void record(ExchangeRateChange exchangeRateChange) {
                recordedExchangeRates.add(exchangeRateChange);
            }

            @Override
            public List<FlaggedChange> getFlaggedChanges() {
                return null;//ignored
            }

            @Override
            public Map<String, Double> getAveragesByMonth(CurrencyCode currencyCode) {
                return null;//ignored
            }

            @Override
            public Map<Integer, Double> getAveragesByYear(CurrencyCode currencyCode) {
                return null;//ignored
            }
        };
        this.stringParser = new StringParser(dataStoreStub);
    }

    @Test
    void shouldFailToReceiveFileWithoutStartOfFile() {
        String emptyFirstLine = "invalid value";
        InvalidExchangeRateFileException exception = callReceiveAndCaptureException(emptyFirstLine);
        String message = exception.getMessage();
        assertEquals("Expected 'START-OF-FILE' on line 1, found 'invalid value'", message);
    }

    /**
     * For the purpose of the exercise I am assuming that the field list wouldn't change order, nor would fields
     * be added or removed.
     */
    @Test
    void shouldReceiveFileGivenTrailingAdditionalParameters() throws InvalidExchangeRateFileException {
        stringParser.receiveFile("START-OF-FILE\n" +
                "DATE=20181015\n" +
                "ADDITIONAL=parameter\n" +
                "START-OF-FIELD-LIST\n" +
                "CURRENCY\n" +
                "EXCHANGE_RATE\n" +
                "LAST_UPDATE\n" +
                "END-OF-FIELD-LIST\n" +
                "START-OF-EXCHANGE-RATES\n" +
                "CHF|0.9832|17:12:59 10/14/2018|\n" +
                "END-OF-EXCHANGE-RATES\n" +
                "END-OF-FILE");

        assertEquals(1, dataStoreStub.getExchangeRateChanges().size());
    }

    @Test
    void shouldReceiveFileGivenBlankLinesInFile() throws InvalidExchangeRateFileException {
        stringParser.receiveFile("\nSTART-OF-FILE\n\n" +
                "DATE=20181015\n\n" +
                "ADDITIONAL=parameter\n\n" +
                "START-OF-FIELD-LIST\n\n" +
                "CURRENCY\n\n" +
                "EXCHANGE_RATE\n\n" +
                "LAST_UPDATE\n\n" +
                "END-OF-FIELD-LIST\n\n" +
                "START-OF-EXCHANGE-RATES\n\n" +
                "CHF|0.9832|17:12:59 10/14/2018|\n\n" +
                "END-OF-EXCHANGE-RATES\n\n" +
                "END-OF-FILE\n\n");

        assertEquals(1, dataStoreStub.getExchangeRateChanges().size());
    }

    @Test
    void shouldReceiveFileGivenWindowsStyleLineEndings() throws InvalidExchangeRateFileException {
        stringParser.receiveFile("START-OF-FILE\r\n" +
                "DATE=20181015\r\n" +
                "ADDITIONAL=parameter\r\n" +
                "START-OF-FIELD-LIST\r\n" +
                "CURRENCY\r\n" +
                "EXCHANGE_RATE\r\n" +
                "LAST_UPDATE\r\n" +
                "END-OF-FIELD-LIST\r\n" +
                "START-OF-EXCHANGE-RATES\r\n" +
                "CHF|0.9832|17:12:59 10/14/2018|\r\n" +
                "END-OF-EXCHANGE-RATES\r\n" +
                "END-OF-FILE\r\n");

        assertEquals(1, dataStoreStub.getExchangeRateChanges().size());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReceiveFileGivenManyRateChanges() throws InvalidExchangeRateFileException {
        stringParser.receiveFile("START-OF-FILE\n" +
                "DATE=20181015\n" +
                "START-OF-FIELD-LIST\n" +
                "CURRENCY\n" +
                "EXCHANGE_RATE\n" +
                "LAST_UPDATE\n" +
                "END-OF-FIELD-LIST\n" +
                "START-OF-EXCHANGE-RATES\n" +
                "CHF|0.9832|17:12:59 10/14/2018|\n" +
                "GBP|0.7849|17:12:59 10/14/2018|\n" +
                "EUR|0.8677|17:13:00 10/14/2018|\n" +
                "CAD|0.9999|13:13:00 02/11/2019|\n" +
                "END-OF-EXCHANGE-RATES\n" +
                "END-OF-FILE");
        Set<ExchangeRateChange> exchangeRateChanges = new HashSet(Arrays.asList(
                new ExchangeRateChange(
                        "CHF",
                        LocalDateTime.parse("2018-10-14T17:12:59"),
                        0.9832),
                new ExchangeRateChange(
                        "GBP",
                        LocalDateTime.parse("2018-10-14T17:12:59"),
                        0.7849),
                new ExchangeRateChange(
                        "EUR",
                        LocalDateTime.parse("2018-10-14T17:13:00"),
                        0.8677),
                new ExchangeRateChange(
                        "CAD",
                        LocalDateTime.parse("2019-02-11T13:13:00"),
                        0.9999)
        ));
        assertTrue(dataStoreStub.getExchangeRateChanges().containsAll(exchangeRateChanges));
        assertEquals(4, dataStoreStub.getExchangeRateChanges().size());
    }

    @Test
    void shouldFailToReceiveGivenMissingEndOfExchangeRates() {
        String fileWithMissingEndOfExchangeRates = "START-OF-FILE\n" +
                "DATE=20181015\n" +
                "START-OF-EXCHANGE-RATES\n" +
                "CHF|0.9832|17:12:59 10/14/2018|\n" +
                "END-OF-FILE";
        InvalidExchangeRateFileException exception = callReceiveAndCaptureException(fileWithMissingEndOfExchangeRates);
        String message = exception.getMessage();
        assertEquals("Unexpected exchange rate format found unable to parse 'END-OF-FILE' on line 5", message);
    }

    @Test
    void shouldFailToReceiveGivenMissingEndOfFile() {
        String fileWithMissingEndOfExchangeRates = "START-OF-FILE\n" +
                "DATE=20181015\n" +
                "START-OF-EXCHANGE-RATES\n" +
                "CHF|0.9832|17:12:59 10/14/2018|\n" +
                "END-OF-EXCHANGE-RATES";
        InvalidExchangeRateFileException exception = callReceiveAndCaptureException(fileWithMissingEndOfExchangeRates);
        String message = exception.getMessage();
        assertEquals("Unexpected end of file", message);
    }

    private InvalidExchangeRateFileException callReceiveAndCaptureException(String file) {
        return assertThrows(InvalidExchangeRateFileException.class, () -> stringParser.receiveFile(file));
    }

}