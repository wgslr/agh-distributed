package currencytracker;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CancellationException;

public class ExchangeRatesService extends ExchangeRatesGrpc.ExchangeRatesImplBase {
    @Override
    public void subscribe(SubscribeArgs request,
                          StreamObserver<Rate> responseObserver) {
        Currency base = request.getBase();

        // TODO use shared object
        RatesProvider ratesProvider = RatesProvider.getInstance();

        System.out.println("Sending initial currency rates information");
        for (Currency c : request.getTrackedList()) {
            double rate = ratesProvider.getRate(base, c);

            Rate.Builder builder = Rate.newBuilder();
            builder.setBase(base)
                    .setForeign(c)
                    .setForeignToBase(rate);
            responseObserver.onNext(builder.build());
        }


        ratesProvider.addChangeListener(change -> {
            if (!request.getTrackedList().contains(change.currency)) {
                return;
            }

            System.out.println("Sending update for currency " + change.currency);
            Rate.Builder builder = Rate.newBuilder();
            builder.setBase(base)
                    .setForeign(change.currency)
                    .setForeignToBase(change.newRate);

            try {
                responseObserver.onNext(builder.build());
            } catch (StatusRuntimeException exception) {
                if (!exception.getStatus().getCode().equals(Status.Code.CANCELLED)) {
                    throw exception;
                }
            }
        });

//        responseObserver.onCompleted();
    }
}
