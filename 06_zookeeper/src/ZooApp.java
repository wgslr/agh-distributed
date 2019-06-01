import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
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

        Thread.sleep(60 * 1000);
    }

}
