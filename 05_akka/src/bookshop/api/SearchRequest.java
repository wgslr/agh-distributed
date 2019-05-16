package bookshop.api;

import akka.actor.ActorRef;

public class SearchRequest extends Request {
    public final String title;

    public SearchRequest(String title) {
        super(null);
        this.title = title;
    }

    public SearchRequest(ActorRef replyTo, String title) {
        super(replyTo);
        this.title = title;
    }
}
