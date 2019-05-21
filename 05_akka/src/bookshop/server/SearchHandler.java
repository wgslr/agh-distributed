package bookshop.server;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import bookshop.api.ErrorResponse;
import bookshop.api.SearchRequest;
import bookshop.api.SearchResult;
import scala.concurrent.duration.Duration;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static akka.actor.SupervisorStrategy.stop;

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
                .match(Terminated.class,
                        t -> {
                            log.info("Child died: " + t.actor().toString());
                            getSelf().tell(
                                    new ErrorResponse(ErrorResponse.ErrorType.DB_UNAVAILABLE),
                                    getSelf()
                            );
                        }
                )
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    @Override
    public void preStart() {
        log.info("Starting");
        dbActors.addAll(Arrays.asList(
                context().actorOf(Props.create(BookDbReader.class, dbPath1), "dbReader1"),
                context().actorOf(Props.create(BookDbReader.class, dbPath2), "dbReader2")
        ));
        dbActors.forEach(actor -> getContext().watch(actor));
    }

    private static SupervisorStrategy strategy
            = new OneForOneStrategy(10, Duration.Inf(), DeciderBuilder.
            matchAny(o -> stop()).
            build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    private void startSearch(SearchRequest request) {
        replyTo = request.replyTo;
        dbActors.forEach(actor -> actor.tell(request, getSelf()));
    }


    private void handleSuccess(SearchResult result) {
        log.info("Search successful");
        replyTo.tell(result, getSelf());
        getContext().stop(getSelf());
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
            getContext().stop(getSelf());
        }
    }
}
