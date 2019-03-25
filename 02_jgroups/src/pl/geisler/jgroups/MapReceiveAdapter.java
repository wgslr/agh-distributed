package pl.geisler.jgroups;

import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import java.util.HashMap;

public class MapReceiveAdapter extends ReceiverAdapter {
    HashMap<String, Integer> store;

    public MapReceiveAdapter(HashMap<String, Integer> store) {
        this.store = store;
    }

    @Override
    public void receive(Message msg) {
        super.receive(msg);
        System.out.println("received message from " + msg.getSrc() + ": " + msg.getObject());
    }

    @Override
    public void viewAccepted(View view) {
        super.viewAccepted(view);
        System.out.println("View accepted: " + view.toString());
    }
}
