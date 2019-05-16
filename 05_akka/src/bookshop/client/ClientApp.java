package bookshop.client;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import bookshop.api.SearchRequest;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class ClientApp {
    private static ActorSelection serverEndpoint;
    public static void main(String[] args) throws Exception {
        final File configFile = new File("client.conf");
        final Config config = ConfigFactory.parseFile(configFile);
        final ActorSystem system = ActorSystem.create("bookshopclient", config);

        System.out.println("Client ActorSystem set up");
        serverEndpoint =
                system.actorSelection("akka.tcp://bookshopserver@127.0.0.1:3552/user/api");

        System.out.println("Starting REPL. Available commands:\n" +
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
                handleSearch(split[1]);
                break;
            default:
                System.out.println(String.format("Unknown command '%s'", split[0]));
                break;
        }
    }

    private static void handleSearch(String title) {
        SearchRequest request = new SearchRequest(title);
        // TODO move into an actor, send self
        serverEndpoint.tell(request, null);
    }
}

