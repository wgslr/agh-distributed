package pl.geisler.jgroups;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MergeView;
import org.jgroups.View;

import java.util.LinkedList;
import java.util.List;

public class MergeHandler implements Runnable {

    private final MergeView mergeView;
    private final JChannel channel;

    public MergeHandler(MergeView view, JChannel channel) {
        mergeView = view;
        this.channel = channel;
    }

    @Override
    public void run() {
        System.err.println("MergeHandler run");

        List<View> subgroups = new LinkedList<>(mergeView.getSubgroups());
        subgroups.sort((gr1, gr2) -> {
            if (gr1.size() == gr2.size()) {
                return gr1.compareTo(gr2);
            } else {
                return gr1.size() - gr2.size();
            }
        });
        View main = subgroups.get(0);
        Address localAddr = channel.getAddress();

        try {
            if (!main.getMembers().contains(localAddr)) {
                System.err.println("Resetting node after partitions merge");
                channel.getState(null, 0);
            } else {
                System.err.println("Node is in the main partition after merge");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
