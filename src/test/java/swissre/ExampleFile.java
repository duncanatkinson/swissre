package swissre;

public interface ExampleFile {

    String EXAMPLE_FILE = "START-OF-FILE\n" +
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
            "END-OF-FILE";

}
