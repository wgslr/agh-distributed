package bank;

import com.google.common.collect.Lists;
import currencytracker.Currency;
import currencytracker.ExchangeRatesGrpc;
import currencytracker.Rate;
import currencytracker.SubscribeArgs;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;

public class CurrencyTrackerClient {
    private ManagedChannel channel;
    private ExchangeRatesGrpc.ExchangeRatesStub stub;

    public CurrencyTrackerClient(int port) {
        this(ManagedChannelBuilder.forAddress("localhost", port).usePlaintext());
    }

    public CurrencyTrackerClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    }

    public CurrencyTrackerClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        System.out.println("Set up grpc channel");
        stub = ExchangeRatesGrpc.newStub(channel);
        System.out.println("Set up grpc stub");
    }

    public void trackChanges() {
        // TODO customize currency selection
        SubscribeArgs args = SubscribeArgs.newBuilder()
                .setBase(Currency.PLN)
                .addAllTracked(Arrays.asList(Currency.GBP, Currency.HRK))
                .build();

        StreamObserver<Rate> responseObserver = new StreamObserver<Rate>() {
            @Override
            public void onNext(Rate rate) {
                System.out.println(String.format("Received rate update for %s: %f",
                        rate.getForeign(), rate.getForeignToBase()));
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

}
