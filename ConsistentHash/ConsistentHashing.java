// demo how ConsistentHashing works using Sorted tree Map

import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashing<T> {
    SortedMap<Long, T> circle = new TreeMap<>();
    private int replicaCount;

    ConsistentHashing(int capacity, T... nodes){
        this.replicaCount = capacity;

        for(T node : nodes){
            add(node);
        }
    }

    private void add(T node) {
        for(int i=0; i<this.replicaCount; i++) {
            circle.put(getHashValue(node.toString() + i), node);
        }
    }

    private long getHashValue(String  node) {
        return Math.abs(node.hashCode());
    }

    private T get(T key) {
        if(circle.isEmpty()) return null;

        long hash = getHashValue(key.toString());

        if(!circle.containsKey(hash)) {
            SortedMap<Long, T> tailMap = circle.tailMap(hash);
            hash = tailMap.size() == 0 ? circle.firstKey() : tailMap.firstKey();
        }

        return circle.get(hash);
    }

    private void remove(T node){
        long hash = getHashValue(node.toString());
        circle.remove(hash);
    }

    public static void main(String[] args) {
        ConsistentHashing<String> consistentHash = new ConsistentHashing<>(3, "node1", "node2", "node3");

        System.out.println("Node for key 'apple': " + consistentHash.get("apple"));
        System.out.println("Node for key 'banana': " + consistentHash.get("banana"));
        System.out.println("Node for key 'cherry': " + consistentHash.get("cherry"));

        consistentHash.add("node4");

        System.out.println("Node for key 'apple' after adding node4: " + consistentHash.get("apple"));
        System.out.println("Node for key 'banana' after adding node4: " + consistentHash.get("banana"));
        System.out.println("Node for key 'cherry' after adding node4: " + consistentHash.get("cherry"));

        consistentHash.remove("node2");

        System.out.println("Node for key 'apple' after removing node2: " + consistentHash.get("apple"));
        System.out.println("Node for key 'banana' after removing node2: " + consistentHash.get("banana"));
        System.out.println("Node for key 'cherry' after removing node2: " + consistentHash.get("cherry"));

    }
}
