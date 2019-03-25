package pl.geisler.jgroups;

public class DistributedMapCli {
    public static void main(String args[]) throws Exception {
        DistributedMap theMap = new DistributedMap();

        theMap.put("first", 111);

        Thread.sleep(15000);


    }
}
