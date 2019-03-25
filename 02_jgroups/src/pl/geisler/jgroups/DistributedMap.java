package pl.geisler.jgroups;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.ProtocolStack;

import java.util.HashMap;

public class DistributedMap implements SimpleStringMap {
    public final static String CLUSTER_NAME = "geislerDistributedMap";

    private JChannel commChannel;

    // TODO init from remote state
    private HashMap<String, Integer> store = new HashMap<>();


    public DistributedMap() throws Exception {
        initCommunication();
    }

    @Override
    public boolean containsKey(String key) {
        return false;
    }

    @Override
    public Integer get(String key) {
        return null;
    }

    @Override
    public void put(String key, Integer value) {
        String msg = String.format("%s: %d", key, value);
        Message jMsg = new Message(null, null, msg);
        try {
            commChannel.send(jMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer remove(String key) {
        return null;
    }


    private void initCommunication() throws Exception {
        ReceiverAdapter adapter = new MapReceiveAdapter(store);

        commChannel = new JChannel(false);

        ProtocolStack stack=new ProtocolStack();
        commChannel.setProtocolStack(stack);
        stack.addProtocol(new UDP())
                .addProtocol(new PING())
                .addProtocol(new MERGE3())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK2())
                .addProtocol(new UNICAST3())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new FRAG2());

        stack.init();

        commChannel.setReceiver(adapter);

        commChannel.connect(CLUSTER_NAME);
    }
}
