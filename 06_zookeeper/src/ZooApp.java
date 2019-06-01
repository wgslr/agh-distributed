import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class ZooApp {
    public static void main(String[] args) throws IOException, InterruptedException,
            KeeperException {
        System.out.println("Args: " + Arrays.toString(args));

        String addr;
        String[] toSpawn = new String[]{"gnome-calculator"};
        if (args.length < 1) {
            addr = "localhost:2181";
        } else {
            addr = args[0];
            toSpawn = Arrays.copyOfRange(args, 1, args.length);
        }

        ApplicationSupervisor as = new ApplicationSupervisor(addr, toSpawn);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println(
                "Available commands:\nprint - print tree of /z\nquit - exit the app");
        while (true) {
            String line = br.readLine().trim();
            if (line.length() == 0)
                continue;

            if (line.charAt(0) == 'p') {
                as.printSubtree("/z");
            } else if (line.charAt(0) == 'q') {
                break;
            }
        }
    }

}
