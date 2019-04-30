package currencytracker;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class CurrencyTrackerServer  {
    private int port;
    private Server server;

    public CurrencyTrackerServer(int port) throws IOException {
        this(ServerBuilder.forPort(port), port);
    }

    public CurrencyTrackerServer(ServerBuilder<?> serverBuilder, int port) {
        this.port = port;
        server = serverBuilder.addService(new ExchangeRatesService())
                .build();
    }

    public void start() throws IOException {
        server.start();
        System.out.println("Server started, listening on " + port);
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
