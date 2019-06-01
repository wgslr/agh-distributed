import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.function.Consumer;

public class ZooWatcher implements Watcher {
    // function consuming a path
    private Consumer<String> onChildrenChanged;

    public void setOnChildrenChanged(Consumer<String> onChildrenChanged) {
        this.onChildrenChanged = onChildrenChanged;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        System.out.println("Event happened: " + watchedEvent);

        final String eventPath = watchedEvent.getPath();

        switch (watchedEvent.getType()) {
            case NodeChildrenChanged:
                onChildrenChanged.accept(eventPath);
                break;
            default:
                break;
        }
    }
}
