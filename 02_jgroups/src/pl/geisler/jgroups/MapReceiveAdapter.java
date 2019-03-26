package pl.geisler.jgroups;

import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class MapReceiveAdapter extends ReceiverAdapter {
    final private HashMap<String, Integer> store;

    public MapReceiveAdapter(HashMap<String, Integer> store) {
        this.store = store;
    }

    @Override
    public void receive(Message msg) {
        try {
            ProtocolMessage protoMsg = (ProtocolMessage) Util.objectFromByteBuffer(msg.getBuffer());
            System.out.println(
                    "received message from " + msg.getSrc() + ": " + protoMsg.toString());
            handleMessage(protoMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (store) {
            Util.objectToStream(store, new DataOutputStream(output));
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        synchronized (store) {
            DataInputStream inputStream = new DataInputStream(input);
            HashMap<String, Integer> state = (HashMap<String, Integer>)
                    Util.objectFromStream(inputStream);
            store.clear();
            store.putAll(state);
        }
    }

    @Override
    public void viewAccepted(View view) {
        super.viewAccepted(view);
        System.out.println("View accepted: " + view.toString());
    }

    private void handleMessage(ProtocolMessage protoMsg) {
        switch (protoMsg.type) {
            case PUT:
                System.err.println(String.format("Put %s=%d", protoMsg.key, protoMsg.value));
                store.put(protoMsg.key, protoMsg.value);
                break;
            case REMOVE:
                System.err.println(String.format("Remove %s", protoMsg.key));
                store.remove(protoMsg.key);
                break;
        }
    }
}
