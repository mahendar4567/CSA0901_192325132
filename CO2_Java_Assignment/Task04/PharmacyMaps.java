import java.util.*;

public class PharmacyMaps {
    public static void main(String[] args) {
        Map<String, Integer> hash = new HashMap<>();
        Map<String, Integer> tree = new TreeMap<>();
        Map<String, Integer> linked = new LinkedHashMap<>();
        String[] codes = {"MED104", "MED101", "MED103", "MED102"};
        int[] stocks = {25, 40, 18, 32};

        for (int i = 0; i < codes.length; i++) {
            hash.put(codes[i], stocks[i]);
            tree.put(codes[i], stocks[i]);
            linked.put(codes[i], stocks[i]);
        }

        System.out.println("HashMap (no guaranteed order): " + hash);
        System.out.println("TreeMap (code order): " + tree);
        System.out.println("LinkedHashMap (insertion order): " + linked);

        long start = System.nanoTime();
        int total = 0;
        for (int i = 0; i < 100000; i++) total += hash.get("MED101");
        long elapsed = System.nanoTime() - start;
        System.out.println("100000 HashMap lookups ns: " + elapsed);
        System.out.println("Lookup check total: " + total);
    }
}
