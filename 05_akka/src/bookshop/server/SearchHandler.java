package bookshop.server;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import bookshop.api.ErrorResponse;
import bookshop.api.SearchRequest;
import bookshop.api.SearchResult;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SearchHandler extends AbstractActor {

    private static final String dbPath1 = "./assets/db1";
    private static final String dbPath2 = "./assets/db2";

    private List<ActorRef> dbActors = new LinkedList<>();
    private List<ErrorResponse> errors = new LinkedList<>();
    private ActorRef replyTo;

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequest.class, this::startSearch)
                .match(SearchResult.class, this::handleSuccess)
                .match(ErrorResponse.class, this::handleError)
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    @Override
    public void preStart() throws Exception {
        dbActors.addAll(Arrays.asList(
                context().actorOf(Props.create(BookDbReader.class, dbPath1), "dbReader1"),
                context().actorOf(Props.create(BookDbReader.class, dbPath2), "dbReader1")
        ));
    }

    private void startSearch(SearchRequest request) {
        replyTo = request.replyTo;
        dbActors.forEach(actor -> actor.tell(request, getSelf()));
    }


    private void handleSuccess(SearchResult result) {
        replyTo.tell(result, getSelf());
        context().stop();
    }

    private void handleError(ErrorResponse err) {
        errors.add(err);
        if (errors.size() == dbActors.size()) {
            Optional<ErrorResponse> notFound = errors.stream()
                    .filter(e -> e.errorType == ErrorResponse.ErrorType.NOT_FOUND)
                    .findAny();
            if (notFound.isPresent()) {
                replyTo.tell(notFound.get(), getSelf());
            } else {
                replyTo.tell(new ErrorResponse(ErrorResponse.ErrorType.DB_UNAVAILABLE), getSelf());
            }
        }
    }
}
