import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class ApplicationSupervisor {
    private static final int SESSION_TIMEOUT = 3000;
    private static final String MAIN_NODE = "/z";

    private ZooKeeper zooKeeper;
    private ZooWatcher watcher;
    private Process spawned = null;

    private int previousChildCount = 0;

    public ApplicationSupervisor(final String address) throws IOException, KeeperException,
            InterruptedException {
        watcher = new ZooWatcher();
        watcher.setOnChildrenChanged(path -> {
            try {
                onChildrenChanged(path);
            } catch (KeeperException | InterruptedException ignore) {
            }
        });
        zooKeeper = new ZooKeeper(address, SESSION_TIMEOUT, watcher);

//
//
//        // set up watches
//        System.out.println("/ze");
//        printSubtree("/ze");
        zooKeeper.getChildren("/", watcher);
        zooKeeper.getChildren("/z", watcher);
    }


    private void onChildrenChanged(final String path) throws KeeperException, InterruptedException {
        // restore watch, asynchronously
        zooKeeper.getChildren(path, watcher, (x, y, z, a, b) -> {
        }, null);
        if (path.equals(MAIN_NODE)) {
            printChildCount(MAIN_NODE);
        } else if (path.equals("/")) {
            refreshState();
        }
    }

    private void refreshState() {
        System.out.println("Something changed in namespace root");
    }


    private void printChildCount(final String path) throws KeeperException, InterruptedException {
        final int count = zooKeeper.getChildren(path, false).size();
        if (count > previousChildCount) {
            // the children count is to be displayed only when adding a child
            System.out.println(String.format("There are %d children of '%s'", count, path));
        }
        previousChildCount = count;
    }

    // TODO remember to print tree root beforehand
    private void printSubtree(final String path) throws KeeperException, InterruptedException {
        for (String child : zooKeeper.getChildren(path, false)) {
            final String childPath = path + "/" + child;
            System.out.println(childPath);
            printSubtree(childPath);
        }

    }


}
