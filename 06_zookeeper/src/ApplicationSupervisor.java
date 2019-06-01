import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class ApplicationSupervisor {
    private static final int SESSION_TIMEOUT = 3000;
    private ZooKeeper zooKeeper;
    private ZooWatcher watcher;

    public ApplicationSupervisor(final String address) throws IOException {
        watcher = new ZooWatcher();
        zooKeeper = new ZooKeeper(address, SESSION_TIMEOUT, watcher);
    }

}
