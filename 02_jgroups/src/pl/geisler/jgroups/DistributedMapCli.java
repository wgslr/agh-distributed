package pl.geisler.jgroups;

import java.util.Scanner;

public class DistributedMapCli {
    public static void main(String args[]) throws Exception {
        DistributedMap theMap = new DistributedMap();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {

                String operation = scanner.next();
                String key;
                Integer value;

                switch (operation.toLowerCase()) {
                    case "put":
                        key = scanner.next();
                        value = scanner.nextInt();
                        theMap.put(key, value);
                        break;
                    case "dump":
                        System.out.println(theMap.toString());
                        break;
                    case "get":
                        key = scanner.next();
                        System.out.println(theMap.get(key));
                        break;
                    case "exists":
                        key = scanner.next();
                        System.out.println(theMap.containsKey(key));
                        break;
                    case "remove":
                        key = scanner.next();
                        System.out.println(theMap.remove(key));
                        break;
                    case "exit":
                        theMap.close();
                        return;
                    default:
                        System.out.println(
                                String.format("Unknown operation '%s'", operation.toLowerCase()));

                }
            } catch (Exception err) {
                err.printStackTrace();
            }
        }


    }
}
