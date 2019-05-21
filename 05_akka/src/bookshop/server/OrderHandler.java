package bookshop.server;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import bookshop.api.*;
import scala.concurrent.duration.Duration;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static akka.actor.SupervisorStrategy.stop;

public class OrderHandler extends AbstractActor {

    private ActorSelection dbActor = getContext().actorSelection("akka://bookshopserver/user" +
            "/orderWriter");
    private ActorRef replyTo;

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrderRequest.class, req -> {
                    replyTo = req.replyTo;
                    dbActor.tell(req, getSelf());
                })
                .match(OrderResult.class, result -> {
                    replyTo.tell(result, getSelf());
                    getContext().stop(getSelf());
                })
                .match(ErrorResponse.class, err -> {
                    replyTo.tell(err, getSelf());
                    getContext().stop(getSelf());
                })
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


    private static SupervisorStrategy strategy
            = new OneForOneStrategy(10, Duration.Inf(), DeciderBuilder.
            matchAny(o -> stop()).
            build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

}
