package bookshop.api;

import akka.actor.ActorRef;

import java.io.Serializable;

public class Request implements Serializable {
    /**
     * Stores the client ref, which allows easy routing in the server
     */
    public ActorRef replyTo;

    public Request(ActorRef replyTo) {
        this.replyTo = replyTo;
    }
}
