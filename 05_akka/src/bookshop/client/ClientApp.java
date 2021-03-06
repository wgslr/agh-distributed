package bookshop.client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import bookshop.api.OrderRequest;
import bookshop.api.SearchRequest;
import bookshop.api.StreamRequest;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class ClientApp {
    private static ActorRef clientActor;

    public static void main(String[] args) throws Exception {
        final File configFile = new File("client.conf");
        final Config config = ConfigFactory.parseFile(configFile);
        final ActorSystem system = ActorSystem.create("bookshopclient", config);

        final String serverPath = "akka.tcp://bookshopserver@127.0.0.1:3552/user/api";
        clientActor = system.actorOf(Props.create(ClientActor.class,
                () -> new ClientActor(serverPath)),
                "client");

        System.out.println("Client ActorSystem set up");
        System.out.println("Starting REPL. Available commands:\n" +
                "o <some title> - order a book\n" +
                "t <some title> - stream a book\n" +
                "s <some title> - lookup a book");


        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = br.readLine();
            if (line.equals("q")) {
                break;
            } else {
                handleCommand(line);
            }
        }

        // finish
        system.terminate();
    }

    private static void handleCommand(String line) {
        final String[] split = line.split("\\s", 2);
        switch (split[0]) {
            case "s":
                SearchRequest searchReq = new SearchRequest(split[1]);
                clientActor.tell(searchReq, null);
                break;
            case "o":
                OrderRequest orderReq = new OrderRequest(split[1]);
                clientActor.tell(orderReq, null);
                break;
            case "t":
                StreamRequest streamReq = new StreamRequest(split[1]);
                clientActor.tell(streamReq, null);
                break;
            default:
                System.out.println(String.format("Unknown command '%s'", split[0]));
                break;
        }
    }
}
