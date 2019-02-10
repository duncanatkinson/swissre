package swissre;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static swissre.ExampleFile.EXAMPLE_FILE;

class StringExchangeRateFileProcessorTest {

    private ExchangeRateFileProcessor<String> stringExchangeRateFileProcessor;

    @BeforeEach
    void setUp() {
        stringExchangeRateFileProcessor = new StringExchangeRateFileProcessor();
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
        String fileWithInvalidDate = "START-OF-FILE\nADDITIONAL=VALUE\nDATE=invalid\n";
        InvalidExchangeRateFileException exception = callReceiveAndCaptureException(fileWithInvalidDate);
        String message = exception.getMessage();
        assertEquals("DATE 'invalid' is not in the format YYYYMMDD on line 3", message);
    }

    @Test
    void shouldReceiveFile() throws InvalidExchangeRateFileException {
        stringExchangeRateFileProcessor.receiveFile(EXAMPLE_FILE);
    }

    private InvalidExchangeRateFileException callReceiveAndCaptureException(String file) {
        return assertThrows(InvalidExchangeRateFileException.class, () -> stringExchangeRateFileProcessor.receiveFile(file));
    }

}