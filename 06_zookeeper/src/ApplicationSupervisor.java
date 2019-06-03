import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;

public class ApplicationSupervisor implements Watcher {
    private static final int SESSION_TIMEOUT = 3000;
    private static final String MAIN_NODE = "/z";

    private ZooKeeper zooKeeper;

    private String[] toSpawn;
    private Process spawned = null;

    private int previousChildCount = 0;

    public ApplicationSupervisor(final String address, final String[] toSpawn) throws IOException,
            KeeperException,
            InterruptedException {
        this.toSpawn = toSpawn;
        zooKeeper = new ZooKeeper(address, SESSION_TIMEOUT, (ignore) -> {
        });

        try {
            zooKeeper.getChildren(MAIN_NODE, false);
            setupChildrenWatch();
            System.out.println(
                    String.format("Node %s exists, set up getChildren watch", MAIN_NODE));
        } catch (KeeperException e) {
            setupExistsWatch();
            System.out.println("Set up watch waiting for /z creation");
        }
    }


    public void printSubtree(final String path) throws KeeperException, InterruptedException {
        if (zooKeeper.exists(path, false) != null) {
            System.out.println(path);
            for (String child : zooKeeper.getChildren(path, false)) {
                printSubtree(path + "/" + child);
            }
        }
    }


    @Override
    public void process(final WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case NodeDeleted:
            case NodeCreated:
                onExistsChanged();
                break;
            case NodeChildrenChanged:
                onChildrenChanged();
                break;
        }
    }


    private void onChildrenChanged() {
        final String path = MAIN_NODE;
        // restore watch and get children count
        setupChildrenWatch();
        printChildCount();
    }


    private void printChildCount() {
        int count = getChildCount(MAIN_NODE);
        System.out.println(String.format("There are %d descendants of '%s'", count, MAIN_NODE));
    }


    private int getChildCount(final String path) {
        try {
            final List<String> children = zooKeeper.getChildren(path, false);
            int count = children.size();
            for (String child : children) {
                count += getChildCount(path + "/" + child);
            }
            return count;
        } catch (KeeperException | InterruptedException e) {
            return 0;
        }
    }


    private void onExistsChanged() {
        try {
            final Stat result = zooKeeper.exists(MAIN_NODE, false);
            if (result == null) {
                setupExistsWatch();
                ensureStopped();
            } else {
                setupChildrenWatch();
                ensureStarted();
            }
        } catch (KeeperException | InterruptedException e) {
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
            }
        }
    }


    private void setupExistsWatch() {
        // asynchronously not to hinder the calling thread
        zooKeeper.exists("/z", this, this::nullStatCallback, null);
    }


    private void setupChildrenWatch() {
        setupChildrenWatch("/z");
    }

    private void setupChildrenWatch(String path) {
        try {
            System.out.println(String.format("Setup watch for '%s'", path));
            for (String child : zooKeeper.getChildren(path, this)) {
                setupChildrenWatch(path + "/" + child);
            }
        } catch (KeeperException | InterruptedException e) {
        }
    }


    private void nullStatCallback(int rc, String path, Object ctx, Stat stat) {
    }


}
