package bookshop.server;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.pattern.Patterns$;
import bookshop.api.SearchRequest;
import bookshop.api.SearchResult;
import scala.concurrent.Future;

import java.awt.print.Book;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchHandler extends AbstractActor {

    private static final String dbPath1 = "./assets/db1";
    private static final String dbPath2 = "./assets/db2";

    private List<ActorRef> dbActors = new LinkedList<>();

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequest.class, this::handleSearch)
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

    private void handleSearch(SearchRequest request) {
        List<scala.concurrent.Future<Object>> futures = dbActors.stream()
                .map(actor -> {
                    Future<Object> future = Patterns.ask(actor, request, 1000 * 60);
                    future.onSuccess();
                })
                .collect(Collectors.toList());
        for (ActorRef actor : dbActors) {
            Patterns.ask(actor, )
        }

        dbActors.forEach(actor -> actor.tell(request, getSelf()));
    }
}
