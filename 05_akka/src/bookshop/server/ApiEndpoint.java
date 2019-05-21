package bookshop.server;

import akka.actor.AbstractActor;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import bookshop.api.OrderRequest;
import bookshop.api.SearchRequest;
import bookshop.api.SearchResult;

public class ApiEndpoint extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequest.class, sr -> {
                    log.info(String.format("Received search request for title %s", sr.title));
                    ActorRef worker = getContext().actorOf(Props.create(SearchHandler.class));
                    worker.tell(sr, self());
                })
                .match(OrderRequest.class, req -> {
                    log.info(String.format("Received order request for title %s", req.title));
                    ActorRef worker = getContext().actorOf(Props.create(OrderHandler.class));
                    worker.tell(req, self());
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }


}
