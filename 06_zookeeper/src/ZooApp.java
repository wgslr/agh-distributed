import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class ZooApp {
    public static void main(String[] args) throws IOException, InterruptedException,
            KeeperException {

        String addr;
        String[] toSpawn;
        if (args.length < 2) {
            System.out.println("Expected arguments: <zookeeperhost> <apptolaunch>...");
            addr = "localhost:2181";
            toSpawn = new String[]{"gnome-calculator"};
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
