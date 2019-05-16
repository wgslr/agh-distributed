package bookshop.client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class ClientApp {
    public static void main(String[] args) throws Exception {
        File configFile = new File("client.conf");
        Config config = ConfigFactory.parseFile(configFile);
        final ActorSystem system = ActorSystem.create("bookshopclient", config);

        System.out.println("Client ActorSystem set up");
        system.actorSelection("akka.tcp://bookshopserver@127.0.0.1:3552/user/api")
                .tell("a mighty request", null);


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

