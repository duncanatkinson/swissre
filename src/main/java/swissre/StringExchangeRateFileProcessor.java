package swissre;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

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

    public StringExchangeRateFileProcessor(ResultsCollector resultsCollector) {
        this.resultsCollector = resultsCollector;
    }

    private Scanner initializeNewScanner(String file) {
        scanner = new Scanner(file);
        scanner.useDelimiter("\\n"); // default is newlines AND whitespace
        lineCounter = new AtomicInteger(0);
        return scanner;
    }

    /**
     * @param file is a state of the art exchange rate file.
     */
    @Override
    public void receiveFile(String file) throws InvalidExchangeRateFileException {
        initializeNewScanner(file);
        ensureNextLineMatches(START_OF_FILE);
        LocalDate fileDate = getFileDate();
        System.out.println("fileDate = " + fileDate);//TODO use this
        ensureNextLineMatches(START_OF_FIELD_LIST);
        while (!nextLineIs(START_OF_EXCHANGE_RATES)) {
            //skip
        }

        resultsCollector.record(getExchangeRateChange());
        resultsCollector.record(getExchangeRateChange());
        resultsCollector.record(getExchangeRateChange());
        System.out.println("scanner.next() = " + scanner.next());
//        System.out.println("scanner.next() = " + scanner.next());
//        System.out.println("scanner.next() = " + scanner.next());
    }

    private ExchangeRateChange getExchangeRateChange() {
        String line = scanner.next();
        String[] parts = line.split("\\|");
        String currency = parts[0];
        Double exchangeRateVsDollar =Double.parseDouble(parts[1]);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss MM/dd/YYYY");

        dateTimeFormatter = new DateTimeFormatterBuilder()
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

        LocalDateTime timestamp = LocalDateTime.parse(parts[2], dateTimeFormatter);
        lineCounter.incrementAndGet();
        return new ExchangeRateChange(currency, timestamp, exchangeRateVsDollar);
    }

    private boolean nextLineIs(ExchangeRateFileToken token) {
        lineCounter.incrementAndGet();
        if (scanner.findInLine(token.asString()) != null) {
            scanner.nextLine();
            return true;
        } else {
            scanner.nextLine();
            return false;
        }
    }

    /**
     * Extracts the date from the head of the exchange rate file after {@link ExchangeRateFileToken#START_OF_FILE}
     * Loops over all the key value pairs in the header in case extra fields are provided unexpectedly.
     *
     * @return the files date from the header
     * @throws InvalidExchangeRateFileException if the date cannot be found or is invalid
     */
    private LocalDate getFileDate() throws InvalidExchangeRateFileException {
        Pattern keyValuePair = Pattern.compile("[\\w ]+=[\\w ]*");
        while (scanner.hasNext(keyValuePair)) {
            String keyValuePairString = scanner.findInLine(keyValuePair);
            scanner.nextLine();
            String[] pair = keyValuePairString.split("=");
            lineCounter.incrementAndGet();
            // for the time being we are only interested in the date
            if (pair[0].equals("DATE")) {
                try {
                    return LocalDate.parse(pair[1], BASIC_ISO_DATE);
                } catch (DateTimeParseException e) {
                    String message = format("DATE ''{0}'' is not in the format YYYYMMDD on line {1}", pair[1], lineCounter.get());
                    throw new InvalidExchangeRateFileException(message);
                }
            } else {
                System.err.println(format("Warning, not handling header parameter {0}", keyValuePairString));
            }
        }
        throw new InvalidExchangeRateFileException("Date expected in header but not found");
    }

    private void ensureNextLineMatches(ExchangeRateFileToken expectedToken) throws InvalidExchangeRateFileException {
        String marker = scanner.findInLine(expectedToken.asString());
        lineCounter.incrementAndGet();
        if (marker == null) {
            String actualLine = "";
            if (scanner.hasNext()) {
                actualLine = scanner.next();
            }
            String message = format("Expected ''{0}'' on line {1}, found ''{2}''", expectedToken.asString(), lineCounter.get(), actualLine);
            throw new InvalidExchangeRateFileException(message);
        } else {
            // matches
            scanner.nextLine();
        }
    }
}
