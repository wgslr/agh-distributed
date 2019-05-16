package bookshop.server;

import akka.actor.AbstractActor;
import akka.actor.Actor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class ApiEndpoint extends AbstractActor{

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(String.class, s -> {
                    log.info("Received request " + s);
                    getSender().tell("result: " + s, getSelf());
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }


}
