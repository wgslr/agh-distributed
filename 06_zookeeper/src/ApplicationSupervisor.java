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
            System.out.println("Setting up children watch");
            List<String> children = zooKeeper.getChildren(MAIN_NODE, this);
            System.out.println("Children: " + children.toString());
        } catch (KeeperException e) {
            System.out.println("Falling back to exists");
            setupExistsWatch();
        }
    }

    // TODO remember to print tree root beforehand
    public void printSubtree(final String path) throws KeeperException, InterruptedException {
        for (String child : zooKeeper.getChildren(path, false)) {
            final String childPath = path + "/" + child;
            System.out.println(childPath);
            printSubtree(childPath);
        }
    }


    @Override
    public void process(final WatchedEvent watchedEvent) {
        System.out.println("Got event: " + watchedEvent);
        assert watchedEvent.getPath().equals("/z");
        switch (watchedEvent.getType()) {
            case NodeDeleted:
            case NodeCreated:
                onExistsChanged(watchedEvent);
                break;
            case NodeChildrenChanged:
                onChildrenChanged(watchedEvent);
                break;
        }
    }

    private void onChildrenChanged(final WatchedEvent event) {
        System.out.println("Received " + event);
        final String path = event.getPath();
        try {
            // restore watch and get children count
            final List<String> children = zooKeeper.getChildren(path, this::onChildrenChanged);
            printChildCount(children.size(), path);
        } catch (KeeperException | InterruptedException e) {
        }
    }


    private void printChildCount(final int count, final String path) {
        if (count > previousChildCount) {
            // the children count is to be displayed only when adding a child
            System.out.println(String.format("There are %d children of '%s'", count, path));
        }
        previousChildCount = count;
    }


    private void onExistsChanged(final WatchedEvent event) {
        System.out.println("onExistsChanged for path " + event.getPath());
        try {
            final Stat result = zooKeeper.exists(event.getPath(), this::onExistsChanged);
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
        zooKeeper.getChildren("/z", this, this::nullChildrenCallback, null);
    }

    private void nullChildrenCallback(int rc, String path, Object ctx, List<String> children) {
    }

    private void nullStatCallback(int rc, String path, Object ctx, Stat stat) {
    }


}
