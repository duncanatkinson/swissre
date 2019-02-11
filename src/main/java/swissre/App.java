package swissre;

import java.util.Arrays;

public class App {

    private final DataStore dataStore;
    private final Parser<String> parser;

    public App() {
        dataStore = new SimpleDataStore();
        parser = new StringParser(dataStore);
    }

    private void run(String[] args) {
        Arrays.asList(args)
                .forEach(parser::receiveFile);
    }

    public static void main(String[] args) {
        App app = new App();
        app.run(args);
    }
}
