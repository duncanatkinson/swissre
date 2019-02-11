package swissre;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StringExchangeRateFileParserTest {

    private ExchangeRateFileProcessor<String> stringExchangeRateFileProcessor;

    private DataStore dataStoreStub;

    @BeforeEach
    void setUp() {
        this.dataStoreStub = new DataStore() {

            Set<ExchangeRateChange> recordedExchangeRates = new HashSet<>();

            @Override
            public Set<ExchangeRateChange> getExchangeRates() {
                return recordedExchangeRates;
            }

            @Override
            public void record(ExchangeRateChange exchangeRateChange, LocalDate fileDate) {
                recordedExchangeRates.add(exchangeRateChange);
            }
        };
        this.stringExchangeRateFileProcessor = new StringExchangeRateFileParser(dataStoreStub);
    }

    @Test
    void shouldFailToReceiveFileWithoutStartOfFile() {
        String emptyFirstLine = "invalid value";
        InvalidExchangeRateFileException exception = callReceiveAndCaptureException(emptyFirstLine);
        String message = exception.getMessage();
        assertEquals("Expected 'START-OF-FILE' on line 1, found 'invalid value'", message);
    }

    @Test
    void shouldFailToReceiveFileWithMissingDate() {
        String fileWithInvalidDate = "START-OF-FILE\nSTART-OF-FIELD-LIST";
        InvalidExchangeRateFileException exception = callReceiveAndCaptureException(fileWithInvalidDate);
        String message = exception.getMessage();
        assertEquals("Date expected in header but not found", message);
    }

    @Test
    void shouldFailToReceiveFileWithInvalidDate() {
        String fileWithInvalidDate = "START-OF-FILE\nDATE=invalid value\n";
        InvalidExchangeRateFileException exception = callReceiveAndCaptureException(fileWithInvalidDate);
        String message = exception.getMessage();
        assertEquals("DATE 'invalid value' is not in the format YYYYMMDD on line 2", message);
    }

    @Test
    void shouldFailToReceiveFileWithInvalidDateGivenAdditionalHeaderParameters() {
        String fileWithInvalidDate = "START-OF-FILE\nADDITIONAL=VALUE\nDATE=invalid value\n";
        InvalidExchangeRateFileException exception = callReceiveAndCaptureException(fileWithInvalidDate);
        String message = exception.getMessage();
        assertEquals("DATE 'invalid value' is not in the format YYYYMMDD on line 3", message);
    }

    @Test
    void shouldReceiveFileGivenTrailingAdditionalParameters() throws InvalidExchangeRateFileException {
        stringExchangeRateFileProcessor.receiveFile("START-OF-FILE\n" +
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

        assertEquals(1, dataStoreStub.getExchangeRates().size());
    }

    @Test
    void shouldReceiveFileGivenBlankLinesInFile() throws InvalidExchangeRateFileException {
        stringExchangeRateFileProcessor.receiveFile("\nSTART-OF-FILE\n\n" +
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

        assertEquals(1, dataStoreStub.getExchangeRates().size());
    }

    @Test
    void shouldReceiveFileGivenWindowsStyleLineEndings() throws InvalidExchangeRateFileException {
        stringExchangeRateFileProcessor.receiveFile("START-OF-FILE\r\n" +
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

        assertEquals(1, dataStoreStub.getExchangeRates().size());
    }

    @Test
    void shouldReceiveFileGivenManyRateChanges() throws InvalidExchangeRateFileException {
        stringExchangeRateFileProcessor.receiveFile("START-OF-FILE\n" +
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
        assertTrue(dataStoreStub.getExchangeRates().containsAll(exchangeRateChanges));
        assertEquals(4, dataStoreStub.getExchangeRates().size());
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
        return assertThrows(InvalidExchangeRateFileException.class, () -> stringExchangeRateFileProcessor.receiveFile(file));
    }

}