package bank;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Identity;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

public class BankServer {
    public void start(String[] args) {
        CurrencyTrackerClient currencyTrackerClient = new CurrencyTrackerClient(20000);
        currencyTrackerClient.trackChanges();


        int status = 0;
        Communicator communicator = null;

        String port = args.length > 0 ? args[0] : "10000";

        try {
            // 1. Inicjalizacja ICE - utworzenie communicatora
            communicator = Util.initialize(args);

            // 2. Konfiguracja adaptera
            // METODA 1 (polecana produkcyjnie): Konfiguracja adaptera Adapter1 jest w pliku
            // konfiguracyjnym podanym jako parametr uruchomienia serwera
            //Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Adapter1");

            // METODA 2 (niepolecana, dopuszczalna testowo): Konfiguracja adaptera Adapter1 jest
            // w kodzie ródłowym
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("Adapter1",
                    String.format("tcp -h localhost -p %s:udp -h localhost -p %s", port, port));

//            // 3. Stworzenie serwanta/serwantów
//            AccountI accountServant = new AccountI("somepesel", "w",
//                    new MoneyAmount(3, Currency.PLN));

            // TODO custom error for creating existing account

            AccountFactoryI factory = new AccountFactoryI();
            adapter.add(factory, new Identity("accountfactory", "accfac"));


//            locator.createAccount("jan", "kowalski", "somepesel", new MoneyAmount(3000,
//                    Currency.PLN));
            // 4. Dodanie wpisów do tablicy ASM
//            adapter.add(accountServant, new Identity("somepesel", "standard"));


            // 5. Aktywacja adaptera i przejcie w pętlę przetwarzania żšdań
            adapter.activate();

            System.out.println("Entering event processing loop...");

            communicator.waitForShutdown();

        } catch (Exception e) {
            System.err.println(e);
            status = 1;
        }
        if (communicator != null) {
            // Clean up
            //
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
        BankServer app = new BankServer();
        app.start(args);
    }
}
