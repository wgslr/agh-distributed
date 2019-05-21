package bookshop.server;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import bookshop.api.OrderRequest;
import bookshop.api.OrderResult;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class OrderDbWriter extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private final String dbPath;

    public OrderDbWriter(String dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrderRequest.class, req -> {
                    write(req);
                    req.replyTo.tell(new OrderResult(req.title), getSelf());
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    private void write(OrderRequest req) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dbPath, true))) {
            writer.write(String.format("%s\n", req.title));
        }
    }
}
