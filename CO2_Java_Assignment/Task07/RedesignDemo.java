import java.util.*;

class EmployeeRecord {
    final int id;
    final String name;

    EmployeeRecord(int id, String name) {
        this.id = id;
        this.name = name;
    }
}

public class RedesignDemo {
    private static EmployeeRecord linearFind(List<EmployeeRecord> records, int id) {
        for (EmployeeRecord e : records) {
            if (e.id == id) return e;
        }
        return null;
    }

    public static void main(String[] args) {
        int[] sizes = {1000, 10000, 50000};
        Random random = new Random(7);

        for (int n : sizes) {
            List<EmployeeRecord> records = new ArrayList<>();
            Map<Integer, EmployeeRecord> index = new HashMap<>();
            for (int i = 0; i < n; i++) {
                EmployeeRecord e = new EmployeeRecord(i, "Employee" + i);
                records.add(e);
                index.put(e.id, e);
            }

            int[] queries = new int[10000];
            for (int i = 0; i < queries.length; i++) queries[i] = random.nextInt(n);

            long start = System.nanoTime();
            int found1 = 0;
            for (int id : queries) if (linearFind(records, id) != null) found1++;
            long linearTime = System.nanoTime() - start;

            start = System.nanoTime();
            int found2 = 0;
            for (int id : queries) if (index.get(id) != null) found2++;
            long indexedTime = System.nanoTime() - start;

            System.out.println("\nRecords=" + n + ", queries=" + queries.length);
            System.out.println("Linear search ns: " + linearTime + ", found=" + found1);
            System.out.println("HashMap lookup ns: " + indexedTime + ", found=" + found2);
        }
    }
}
