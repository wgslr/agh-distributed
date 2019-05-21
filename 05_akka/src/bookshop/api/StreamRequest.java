package bookshop.api;

import akka.actor.ActorRef;

public class StreamRequest extends Request {
    public final String title;

    public StreamRequest(String title) {
        super(null);
        this.title = title;
    }

    public StreamRequest(ActorRef replyTo, String title) {
        super(replyTo);
        this.title = title;
    }
}
