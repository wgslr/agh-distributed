package bookshop.client;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class ClientApp {
    public static void main(String[] args) throws Exception {
        final File configFile = new File("client.conf");
        final Config config = ConfigFactory.parseFile(configFile);
        final ActorSystem system = ActorSystem.create("bookshopclient", config);

        System.out.println("Client ActorSystem set up");
        final ActorSelection serverEndpoint =
                system.actorSelection("akka.tcp://bookshopserver@127.0.0.1:3552/user/api");


        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = br.readLine();
            if (line.equals("q")) {
                break;
            }
        }

        // finish
        system.terminate();
    }
}

