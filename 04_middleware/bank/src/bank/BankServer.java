package bank;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Identity;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class BankServer {

    final private int port;
    final private List<Currency> currencies;

    public final static Currency BASE_CURRENCY = Currency.PLN;

    public BankServer(int port, Collection<String> currencies) {
        this.port = port;
        this.currencies = currencies.stream()
                .map(CurrencyTranslator::stringToIce)
                .collect(Collectors.toList());
    }


    public void start(String[] args) {
        CurrencyTrackerClient currencyTrackerClient = new CurrencyTrackerClient(20000, currencies);
        currencyTrackerClient.trackChanges();


        int status = 0;
        Communicator communicator = null;

        try {
            communicator = Util.initialize(args);
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("Adapter1",
                    String.format("tcp -h localhost -p %d:udp -h localhost -p %d", port, port));

            // TODO custom error for creating existing account

            AccountFactoryI factory = new AccountFactoryI(currencyTrackerClient);
            adapter.add(factory, new Identity("accountfactory", "accfac"));

            adapter.activate();
            System.out.println("Entering event processing loop...");
            communicator.waitForShutdown();
        } catch (Exception e) {
            System.err.println(e);
            status = 1;
        }

        if (communicator != null) {
            try {
                communicator.destroy();
            } catch (Exception e) {
                System.err.println(e);
                status = 1;
            }
        }
        System.exit(status);
    }


    public static void main(String[] args) {
        if (args.length < 2 ) {
            displayHelp();
            return;
        }

        List<String> argsList = Arrays.asList(args);
        try {
            int port = Integer.parseInt(argsList.get(0));

            BankServer app = new BankServer(port, argsList.subList(1, argsList.size()));
            app.start(args);
        } catch (IllegalArgumentException e) {
            System.out.print("Invalid argument!");
            displayHelp();
        }
    }


    private static void displayHelp() {
        System.out.println("Usage: <port number> <tracked currency 1> <tracked currency 2> ...");
        System.out.println("Possible currencies: " + Arrays.toString(Currency.values()));
    }
}
