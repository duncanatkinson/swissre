package swissre;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static java.text.MessageFormat.format;
import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;
import static swissre.ExchangeRateFileMarker.START_OF_FIELD_LIST;
import static swissre.ExchangeRateFileMarker.START_OF_FILE;

/**
 * Implementation of the {@link ExchangeRateFileProcessor} for the type {@link String}
 * <p>
 * An instance of this class should be created for each
 * <p>
 * As this is a string processor we can assume the entire file fits in memory.
 *
 * @author Duncan Atkinson
 */
public class StringExchangeRateFileProcessor implements ExchangeRateFileProcessor<String> {


    /**
     * @param file is a state of the art exchange rate file.
     */
    @Override
    public void receiveFile(String file) throws InvalidExchangeRateFileException {
        Scanner scanner = initializeNewScanner(file);
        AtomicInteger lineNumber = new AtomicInteger(0);
        lookForNextMarker(scanner, lineNumber, START_OF_FILE);
        LocalDate fileDate = getFileDate(scanner, lineNumber);
        lookForNextMarker(scanner, lineNumber, START_OF_FIELD_LIST);
    }

    private Scanner initializeNewScanner(String file) {
        Scanner scanner = new Scanner(file);
        scanner.useDelimiter("\\n"); // default is newlines and whitespace
        return scanner;
    }

    /**
     * Loops over all the key value pairs in the header in case extra fields are provided unexpectedly.
     *
     * @param scanner
     * @param lineNumber
     * @return
     * @throws InvalidExchangeRateFileException if the date cannot be found or is invalid
     */
    private LocalDate getFileDate(Scanner scanner, AtomicInteger lineNumber) throws InvalidExchangeRateFileException {
        Pattern keyValuePair = Pattern.compile("[\\w ]+=[\\w ]*");
        while (scanner.hasNext(keyValuePair)) {
            String keyValuePairString = scanner.findInLine(keyValuePair);
            scanner.nextLine();
            String[] pair = keyValuePairString.split("=");
            lineNumber.incrementAndGet();
            if (pair.length != 2) {
                throw new IllegalStateException("Should have only name and value");
            }
            // for the time being we are only interested in the date
            if (pair[0].equals("DATE")) {
                try {
                    return LocalDate.parse(pair[1], BASIC_ISO_DATE);
                } catch (DateTimeParseException e) {
                    String message = format("DATE ''{0}'' is not in the format YYYYMMDD on line {1}", pair[1], lineNumber.get());
                    throw new InvalidExchangeRateFileException(message);
                }
            }
        }
        throw new InvalidExchangeRateFileException("Date expected in header but not found");
    }

    private boolean lookForNextMarker(Scanner scanner, AtomicInteger lineNumber, ExchangeRateFileMarker expectedMarker) throws InvalidExchangeRateFileException {
        String marker = scanner.findInLine(expectedMarker.asString());
        lineNumber.incrementAndGet();
        if (marker == null) {
            String actualLine = "";
            if (scanner.hasNext()) {
                actualLine = scanner.next();
            }
            String message = format("Expected ''{0}'' on line {1}, found ''{2}''", expectedMarker.asString(), lineNumber.get(), actualLine);
            throw new InvalidExchangeRateFileException(message);
        } else {
            scanner.nextLine();
            return marker.equals(expectedMarker.asString());
        }
    }
}
