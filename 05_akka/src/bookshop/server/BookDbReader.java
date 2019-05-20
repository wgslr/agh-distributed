package bookshop.server;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import bookshop.api.ErrorResponse;
import bookshop.api.SearchRequest;
import bookshop.api.SearchResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

public class BookDbReader extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private final String dbPath;

    public BookDbReader(String dbPath) {
        this.dbPath = dbPath;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequest.class, req -> {
                    Optional<ShelfItem> result = lookup(req.title);
                    if (result.isPresent()) {
                        SearchResult sr = new SearchResult(req.title, result.get().price);
                        getSender().tell(sr, getSelf());
                    } else {
                        getSender().tell(
                                new ErrorResponse(ErrorResponse.ErrorType.NOT_FOUND),
                                getSelf());
                    }
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    private Optional<ShelfItem> lookup(String title) throws IOException {
        try (Stream<String> stream = Files.lines(Paths.get(dbPath))) {
            return stream
                    .peek(x -> {
                        try {
                            // reading is supposed to be slow
                            Thread.sleep(100);
                        } catch (InterruptedException ignored) {
                        }
                    })
                    .map(this::parseLine)
                    .filter(si -> si.title.equals(title))
                    .findFirst();
        }
    }

    private ShelfItem parseLine(String dbLine) {
        String[] split = dbLine.split(" ", 2);
        double price = Double.parseDouble(split[0]);
        return new ShelfItem(split[1], price);
    }
}
