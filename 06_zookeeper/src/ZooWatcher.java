import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class ZooWatcher implements Watcher {
    @Override
    public void process(WatchedEvent watchedEvent) {
        System.out.println("Event happened: " + watchedEvent);

    }
}
