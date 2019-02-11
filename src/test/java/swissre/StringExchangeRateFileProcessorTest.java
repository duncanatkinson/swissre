package swissre;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StringExchangeRateFileProcessorTest {

    private ExchangeRateFileProcessor<String> stringExchangeRateFileProcessor;

    private ResultsCollector resultsCollectorStub;

    @BeforeEach
    void setUp() {
        this.resultsCollectorStub = new ResultsCollector() {

            Set<ExchangeRateChange> recordedExchangeRates = new HashSet<>();

            @Override
            public Set<ExchangeRateChange> getExchangeRates() {
                return recordedExchangeRates;
            }

            @Override
            public void record(ExchangeRateChange exchangeRateChange) {
                recordedExchangeRates.add(exchangeRateChange);
            }
        };
        this.stringExchangeRateFileProcessor = new StringExchangeRateFileProcessor(resultsCollectorStub);
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
    void shouldReceiveFile() throws InvalidExchangeRateFileException {
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
                        0.8677)
        ));
        assertTrue(resultsCollectorStub.getExchangeRates().containsAll(exchangeRateChanges));
        assertEquals(3, resultsCollectorStub.getExchangeRates().size());
    }

    private InvalidExchangeRateFileException callReceiveAndCaptureException(String file) {
        return assertThrows(InvalidExchangeRateFileException.class, () -> stringExchangeRateFileProcessor.receiveFile(file));
    }

}