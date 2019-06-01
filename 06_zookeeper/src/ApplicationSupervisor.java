import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;

public class ApplicationSupervisor {
    private static final int SESSION_TIMEOUT = 3000;
    private static final String MAIN_NODE = "/z";

    private ZooKeeper zooKeeper;
    private ZooWatcher watcher;

    private String[] toSpawn;
    private Process spawned = null;

    private int previousChildCount = 0;

    public ApplicationSupervisor(final String address, final String[] toSpawn) throws IOException,
            KeeperException,
            InterruptedException {
        this.toSpawn = toSpawn;
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
        zooKeeper.exists("/z", this::onExistsChanged);
        zooKeeper.getChildren("/z", watcher, (x, y, z, a, b) -> {}, null);
    }

    // TODO remember to print tree root beforehand
    public void printSubtree(final String path) throws KeeperException, InterruptedException {
        for (String child : zooKeeper.getChildren(path, false)) {
            final String childPath = path + "/" + child;
            System.out.println(childPath);
            printSubtree(childPath);
        }
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


    private void onExistsChanged(WatchedEvent event) {
        System.out.println("onExistsChanged for path " + event.getPath());
        try {
            final Stat result = zooKeeper.exists(event.getPath(), this::onExistsChanged);
            if (result == null) {
                ensureStopped();
            } else {
                ensureStarted();
                zooKeeper.getChildren("/z", watcher, (x, y, z, a, b) -> {}, null);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void ensureStopped() {
        if (spawned != null && spawned.isAlive()) {
            spawned.destroy();
        }
    }

    private void ensureStarted() {
        if (spawned == null || !spawned.isAlive()) {
            try {
                spawned = Runtime.getRuntime().exec(toSpawn);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void refreshState() {
        System.out.println("Something changed in namespace root");
//        if zooKeeper.get
    }


    private void printChildCount(final String path) throws KeeperException, InterruptedException {
        final int count = zooKeeper.getChildren(path, false).size();
        if (count > previousChildCount) {
            // the children count is to be displayed only when adding a child
            System.out.println(String.format("There are %d children of '%s'", count, path));
        }
        previousChildCount = count;
    }


}
