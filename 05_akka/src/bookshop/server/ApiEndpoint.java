package bookshop.server;

import akka.actor.AbstractActor;
import akka.actor.Actor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import bookshop.api.SearchRequest;

public class ApiEndpoint extends AbstractActor{

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(String.class, s -> {
                    log.info("Received string request " + s);
                    getSender().tell("result: " + s, getSelf());
                })
                .match(SearchRequest.class, (SearchRequest sr) ->
                        log.info(String.format("Received search request for title %s", sr.title)))
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }


}
