import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.Arrays;

public class ZooApp {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Args: " + Arrays.toString(args));

        String addr;
        if (args.length < 1) {
            addr = "localhost:2181";
        } else {
            addr = args[0];
        }

        ApplicationSupervisor as = new ApplicationSupervisor(addr);

        Thread.sleep(60 * 1000);
    }

}
