package currencytracker;

import io.grpc.stub.StreamObserver;

public class ExchangeRatesService extends ExchangeRatesGrpc.ExchangeRatesImplBase {
    @Override
    public void subscribe(SubscribeArgs request,
                          StreamObserver<Rate> responseObserver) {
        Currency base = request.getBase();

        // TODO use shared object
        RatesProvider ratesProvider = new RatesProvider();

        for (Currency c : request.getTrackedList()) {
            double rate = ratesProvider.getRate(base, c);

            Rate.Builder builder = Rate.newBuilder();
            builder.setBase(base)
                    .setForeign(c)
                    .setForeignToBase(rate);
            responseObserver.onNext(builder.build());
        }

        // TODO asynchronously generate rate changes

        responseObserver.onCompleted();
    }
}
