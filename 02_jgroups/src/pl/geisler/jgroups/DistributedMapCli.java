package pl.geisler.jgroups;

public class DistributedMapCli {
    public static void main(String args[]) throws Exception {
        DistributedMap theMap = new DistributedMap();

        Integer i  = 1;
        while(true) {

            theMap.put("Number" + i.toString(), i);

            ++i;
            Thread.sleep(1000);
        }


    }
}
