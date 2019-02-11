package swissre;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static java.text.MessageFormat.format;
import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;
import static java.time.temporal.ChronoField.*;
import static swissre.ExchangeRateFileToken.*;

/**
 * Implementation of the {@link ExchangeRateFileProcessor} for the type {@link String}
 * <p>
 * Please note that this class is not thread safe.
 * <p>
 * As this is a string processor we can assume the entire file fits in memory.
 *
 * @author Duncan Atkinson
 */
public class StringExchangeRateFileProcessor implements ExchangeRateFileProcessor<String> {


    private final ResultsCollector resultsCollector;

    private Scanner scanner;

    private AtomicInteger lineCounter;
    private final DateTimeFormatter exchangeRateDateTimeFormat;
    private String currentLine;

    public StringExchangeRateFileProcessor(ResultsCollector resultsCollector) {
        this.resultsCollector = resultsCollector;
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

    private void initializeNewScanner(String file) {
        scanner = new Scanner(file);
        scanner.useDelimiter("\\n"); // default is newlines AND whitespace
        lineCounter = new AtomicInteger(0);
    }

    /**
     * @param file is a state of the art exchange rate file.
     */
    @Override
    public void receiveFile(String file) throws InvalidExchangeRateFileException {
        initializeNewScanner(file);
        ensureNextLineMatches(START_OF_FILE);
        LocalDate fileDate = iterateOverFileHeaderAttributes();
        System.out.println("fileDate = " + fileDate);//TODO use this
        while (!currentLineMatches(START_OF_EXCHANGE_RATES)) {
            scanNextLine();
        }
        scanNextLine();
        while (!currentLineMatches(END_OF_EXCHANGE_RATES)) {
            resultsCollector.record(getExchangeRateChangeFromCurrentLine());
            scanNextLine();
        }
        System.out.println("scanner.next() = " + scanner.next());
    }

    private ExchangeRateChange getExchangeRateChangeFromCurrentLine() {
        String[] parts = currentLine.split("\\|");
        String currency = parts[0];
        Double exchangeRateVsDollar = Double.parseDouble(parts[1]);

        LocalDateTime timestamp = LocalDateTime.parse(parts[2], exchangeRateDateTimeFormat);

        return new ExchangeRateChange(currency, timestamp, exchangeRateVsDollar);
    }

    private boolean currentLineMatches(ExchangeRateFileToken token) {
        return token.asString().equals(currentLine);
    }

    private void scanNextLine() {
        currentLine = scanner.next();
        lineCounter.incrementAndGet();
    }

    /**
     * Extracts the date from the head of the exchange rate file after {@link ExchangeRateFileToken#START_OF_FILE}
     * Loops over all the key value pairs in the header in case extra fields are provided unexpectedly.
     *
     * @return the files date from the header
     * @throws InvalidExchangeRateFileException if the date cannot be found or is invalid
     */
    private LocalDate iterateOverFileHeaderAttributes() throws InvalidExchangeRateFileException {
        LocalDate timestamp = null;
        scanNextLine();
        while (currentLine.matches("[\\w ]+=[\\w ]*")) {
            String[] pair = currentLine.split("=");
            // for the time being we are only interested in the date
            if (pair[0].equals("DATE")) {
                timestamp = parseDateFromCurrentLine(pair);
            } else {
                System.err.println(format("Warning, not handling header parameter {0}", currentLine));
            }
            scanNextLine();
        }
        if (timestamp != null) {
            return timestamp;
        } else {
            throw new InvalidExchangeRateFileException("Date expected in header but not found");
        }
    }

    private LocalDate parseDateFromCurrentLine(String[] pair) throws InvalidExchangeRateFileException {
        LocalDate timestamp;
        try {
            timestamp = LocalDate.parse(pair[1], BASIC_ISO_DATE);
        } catch (DateTimeParseException e) {
            String message = format("DATE ''{0}'' is not in the format YYYYMMDD on line {1}", pair[1], lineCounter.get());
            throw new InvalidExchangeRateFileException(message);
        }
        return timestamp;
    }

    private void ensureNextLineMatches(ExchangeRateFileToken expectedToken) throws InvalidExchangeRateFileException {
        String tokenString = expectedToken.asString();
        scanNextLine();

        if (!expectedToken.asString().equals(currentLine)) {
            String message = format("Expected ''{0}'' on line {1}, found ''{2}''", tokenString, lineCounter.get(), currentLine);
            throw new InvalidExchangeRateFileException(message);
        }
    }
}
