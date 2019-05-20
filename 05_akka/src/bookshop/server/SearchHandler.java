package bookshop.server;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import bookshop.api.SearchRequest;
import bookshop.api.SearchResult;

public class SearchHandler extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequest.class, this::handleSearch)
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    private void handleSearch(SearchRequest request) {
        SearchResult result = new SearchResult(request.title, 3.33);
        request.replyTo.tell(result, self());
        // this is a one-off worker
        getContext().stop(getSelf());
    }
}
