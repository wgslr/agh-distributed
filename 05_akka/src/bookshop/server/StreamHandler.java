package bookshop.server;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.ThrottleMode;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import bookshop.api.ErrorResponse;
import bookshop.api.StreamRequest;
import scala.concurrent.duration.FiniteDuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StreamHandler extends AbstractActor {

    private static final String booksFolder = "./assets/books/";

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StreamRequest.class, request -> {
                    try {
                        stream(request);
                    } catch (Exception any) {
                        request.replyTo.tell(
                                new ErrorResponse(ErrorResponse.ErrorType.INTERNAL_SERVER_ERROR),
                                getSelf());
                    }
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    private void stream(StreamRequest request) throws IOException {
        final Materializer materializer = ActorMaterializer.create(getContext());

        final Path path = Paths.get(booksFolder).resolve(request.title);
        List<String> lines = Files.readAllLines(path);
        final Source source = Source.from(lines);
        final Flow throttler = Flow.of(String.class)
                .throttle(1, FiniteDuration.create(1, TimeUnit.SECONDS), 1, ThrottleMode
                        .shaping());
        final Sink sink = Sink.actorRef(request.replyTo, "END OF BOOK " + request.title);


        log.info(String.format("Streaming file %s to %s", path, request.replyTo));
        source
                .via(throttler)
                .to(sink)
                .run(materializer);

    }

}
