package pl.geisler.jgroups;

public class ProtocolMessage {

    private enum Type {
        PUT,
        REMOVE
    }


    final public Type type;
    final public String key;
    final public Integer value;

    private ProtocolMessage(Type type, String key, Integer value) {
        this.type = type;
        this.key = key;
        this.value = 0;
    }

    public static ProtocolMessage makePutMessage(String key, Integer value) {
        return new ProtocolMessage(Type.PUT, key, value);
    }

    public static ProtocolMessage makeRemoveMessage(String key) {
        return new ProtocolMessage(Type.REMOVE, key, null);
    }

}
