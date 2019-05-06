package bank;

import com.google.common.collect.Lists;
import currencytracker.Currency;
import currencytracker.ExchangeRatesGrpc;
import currencytracker.Rate;
import currencytracker.SubscribeArgs;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.*;
import java.util.stream.Collectors;

public class CurrencyTrackerClient {
    private ManagedChannel channel;
    private ExchangeRatesGrpc.ExchangeRatesStub stub;

    final private HashMap<Currency, Double> ratesCache = new HashMap<>();
    final private Set<Currency> trackedCurrencies;

    public CurrencyTrackerClient(int port, Collection<bank.Currency> trackedCurrencies) {
        this(ManagedChannelBuilder.forAddress("localhost", port).usePlaintext(), trackedCurrencies);
    }

    public CurrencyTrackerClient(String host, int port,
                                 Collection<bank.Currency> trackedCurrencies) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(), trackedCurrencies);
    }

    public CurrencyTrackerClient(ManagedChannelBuilder<?> channelBuilder,
                                 Collection<bank.Currency> trackedCurrencies) {
        this.trackedCurrencies = trackedCurrencies.stream()
                .map(CurrencyTranslator::iceToGrpc)
                .collect(Collectors.toSet());

        channel = channelBuilder.build();
        System.out.println("Set up grpc channel");
        stub = ExchangeRatesGrpc.newStub(channel);
        System.out.println("Set up grpc stub");
    }

    public void trackChanges() {
        SubscribeArgs args = SubscribeArgs.newBuilder()
                .setBase(Currency.PLN)
                .addAllTracked(trackedCurrencies)
                .build();

        StreamObserver<Rate> responseObserver = new StreamObserver<Rate>() {
            @Override
            public void onNext(Rate rate) {
                System.out.println(String.format("Received rate update for %s: %f",
                        rate.getForeign(), rate.getForeignToBase()));
                ratesCache.put(rate.getForeign(), rate.getForeignToBase());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(String.format("Observer error: %s", throwable.toString()));
            }

            @Override
            public void onCompleted() {
                System.out.println("Currency rates stream finished");
            }
        };

        stub.subscribe(args, responseObserver);
    }

    public Optional<Double> getRate(bank.Currency currency) {
        Currency grpcCurreny = CurrencyTranslator.iceToGrpc(currency);
        return Optional.ofNullable(ratesCache.get(grpcCurreny));
    }

}
