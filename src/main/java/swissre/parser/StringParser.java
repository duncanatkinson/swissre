package swissre.parser;

import swissre.persistence.DataStore;
import swissre.model.ExchangeRateChange;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static java.text.MessageFormat.format;
import static java.time.temporal.ChronoField.*;
import static swissre.parser.LineMarker.*;

/**
 * Implementation of the {@link Parser} for the type {@link String}
 *
 * Please note that this class is not thread safe.
 *
 * As this is a string processor we can assume the entire file fits in memory.
 *
 * @author Duncan Atkinson
 */
public class StringParser implements Parser<String> {


    private final DataStore dataStore;

    private Scanner scanner;

    private AtomicInteger lineCounter;
    private final DateTimeFormatter exchangeRateDateTimeFormat;
    private String currentLine = "";

    public StringParser(DataStore dataStore) {
        this.dataStore = dataStore;
        exchangeRateDateTimeFormat = new DateTimeFormatterBuilder()
                .appendValue(HOUR_OF_DAY)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR)
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE)
                .appendLiteral(' ')
                .appendValue(MONTH_OF_YEAR)
                .appendLiteral('/')
                .appendValue(DAY_OF_MONTH, 2)
                .appendLiteral('/')
                .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
                .toFormatter();
    }

    private void prepareToProcessFile() {
        scanner.useDelimiter("\\n"); // default is newlines AND whitespace
        lineCounter = new AtomicInteger(0);
    }

    private void initializeNewScanner(String file) {
        scanner = new Scanner(file);
        prepareToProcessFile();
    }

    /**
     * @param file is a state of the art exchange rate file.
     * @throws InvalidExchangeRateFileException if there was an error detected during parsing for example non well formed.
     */
    @Override
    public void receiveFile(String file) throws InvalidExchangeRateFileException {
        initializeNewScanner(file);
        processFile();
    }

    /**
     * Added to this class to allow it to be used directly from the command line
     * instead of via {@link #receiveFile(String)}
     *
     * @param scanner which is expected to receive an exchange rate changes file.
     */
    public void receive(Scanner scanner){
        this.scanner = scanner;
        prepareToProcessFile();
        processFile();
    }

    /**
     * {@link #prepareToProcessFile()} should be called before this method.
     */
    private void processFile() {
        ensureNextLineMatches(START_OF_FILE);

        while (!currentLineMatches(START_OF_EXCHANGE_RATES)) {
            scanNextLine();
        }
        scanNextLine();
        while (!currentLineMatches(END_OF_EXCHANGE_RATES)) {
            ExchangeRateChange exchangeRateChange = getExchangeRateChangeFromCurrentLine();
            dataStore.record(exchangeRateChange);
            scanNextLine();
        }
        ensureNextLineMatches(END_OF_FILE);
    }

    private ExchangeRateChange getExchangeRateChangeFromCurrentLine() {
        String[] parts = currentLine.split("\\|");

        if (parts.length != 3) {
            String message = "Unexpected exchange rate format found unable to parse '" + currentLine + "' on line " + lineCounter.get();
            throw new InvalidExchangeRateFileException(message);

        }

        String currency = parts[0];
        Double exchangeRateVsDollar = Double.parseDouble(parts[1]);

        LocalDateTime timestamp = LocalDateTime.parse(parts[2], exchangeRateDateTimeFormat);

        return new ExchangeRateChange(currency, timestamp, exchangeRateVsDollar);
    }

    private boolean currentLineMatches(LineMarker lineMarker) {
        return lineMarker.asString().equals(currentLine);
    }

    private void scanNextLine() {
        try {
            do {
                currentLine = scanner.next();
                lineCounter.incrementAndGet();
            } while (currentLine.equals(""));
        } catch (NoSuchElementException noSuchElementException) {
            throw new InvalidExchangeRateFileException("Unexpected end of file");
        }
        currentLine = currentLine.replaceAll("\r", "");
    }

    private void ensureNextLineMatches(LineMarker lineMarker) throws InvalidExchangeRateFileException {
        scanNextLine();

        if (!lineMarker.asString().equals(currentLine)) {
            String message = format("Expected ''{0}'' on line {1}, found ''{2}''", lineMarker, lineCounter.get(), currentLine);
            throw new InvalidExchangeRateFileException(message);
        }
    }
}
