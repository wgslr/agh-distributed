package bookshop.client;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import bookshop.api.Request;
import bookshop.api.SearchResult;

/**
 * Actor used for communicating with ApiEndpoint
 */
public class ClientActor extends AbstractActor {

    private final String apiEndpointPath;

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public ClientActor(String apiEndpointPath) {
        this.apiEndpointPath = apiEndpointPath;
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(Request.class, r -> {
                    r.replyTo = getSelf();
                    getContext().actorSelection(apiEndpointPath).tell(r, getSelf());
                })
                .match(SearchResult.class, sr ->
                        System.out.println(String.format("Price of '%s' is %.2f",
                                sr.title, sr.price)))
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }


}
