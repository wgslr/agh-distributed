import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class ApplicationSupervisor {
    private static final int SESSION_TIMEOUT = 3000;
    private static final String MAIN_NODE = "/z";

    private ZooKeeper zooKeeper;
    private ZooWatcher watcher;
    private Process spawned = null;

    public ApplicationSupervisor(final String address) throws IOException, KeeperException,
            InterruptedException {
        watcher = new ZooWatcher();
        zooKeeper = new ZooKeeper(address, SESSION_TIMEOUT, watcher);

        // set up watches
        System.out.println("/ze");
        printSubtree("/ze");
        zooKeeper.getChildren("/", watcher);
    }


    private void onChildrenChanged(final String path) throws KeeperException, InterruptedException {
        // restore watch
        zooKeeper.getChildren(path, watcher, (x,y,z,a,b) -> {}, null);
        if(path.equals(MAIN_NODE)) {
            System.out.println(MAIN_NODE);
            printSubtree(MAIN_NODE);
        } else if (path.equals("/")) {
            refreshState();
        }
    }

    private void refreshState() {

    }


    private void printSubtree(String path) throws KeeperException, InterruptedException {
        for (String child : zooKeeper.getChildren(path, false)) {
            final String childPath = path + "/" + child;
            System.out.println(childPath);
            printSubtree(childPath);
        }

    }


}
