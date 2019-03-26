package pl.geisler.jgroups;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.Util;

import java.net.InetAddress;
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
        return store.containsKey(key);
    }

    @Override
    public Integer get(String key) {
        return store.get(key);
    }

    @Override
    public void put(String key, Integer value) {
        ProtocolMessage msg = ProtocolMessage.makePutMessage(key, value);
        sendMsg(msg);
    }

    @Override
    public Integer remove(String key) {
        Integer removedValue = store.get(key);
        ProtocolMessage msg = ProtocolMessage.makeRemoveMessage(key);
        sendMsg(msg);
        return removedValue;
    }

    public void close() {
        commChannel.close();
    }

    private void initCommunication() throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");

        commChannel = new JChannel(false);
        ReceiverAdapter adapter = new MapReceiveAdapter(store, commChannel);

        ProtocolStack stack = new ProtocolStack();
        commChannel.setProtocolStack(stack);

        UDP udp = new UDP();
        udp.setValue("mcast_group_addr", InetAddress.getByName("230.100.200.97"));
        stack.addProtocol(udp)
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
                .addProtocol(new FRAG2())
                .addProtocol(new STATE())
                .addProtocol(new FLUSH());

        stack.init();

        commChannel.setReceiver(adapter);

        commChannel.connect(CLUSTER_NAME, null, 0);
    }


    private void sendMsg(ProtocolMessage msg) {
        try {
            byte[] buffer = Util.objectToByteBuffer(msg);
            Message jMsg = new Message(null, buffer);
            commChannel.send(jMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "DistributedMap " + store.toString();
    }
}
