package currencytracker;

public class CurrencyTracker {

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 20000;

        CurrencyTrackerServer server = new CurrencyTrackerServer(port);
        server.start();
        server.blockUntilShutdown();
    }
}
