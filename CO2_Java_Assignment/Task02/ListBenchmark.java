import java.util.*;

public class ListBenchmark {
    private static long measure(Runnable r) {
        long start = System.nanoTime();
        r.run();
        return System.nanoTime() - start;
    }

    public static void main(String[] args) {
        int[] sizes = {1000, 10000, 50000};
        for (int n : sizes) {
            List<Integer> a = new ArrayList<>();
            List<Integer> l = new LinkedList<>();
            for (int i = 0; i < n; i++) { a.add(i); l.add(i); }
            final int count = Math.min(n, 100);
            System.out.println("\nSize=" + n);
            System.out.println("ArrayList get: " + measure(() -> {
                long sum = 0; for (int i = 0; i < count; i++) sum += a.get(i);
                if (sum == -1) System.out.println(sum);
            }) + " ns");
            System.out.println("LinkedList get: " + measure(() -> {
                long sum = 0; for (int i = 0; i < count; i++) sum += l.get(i);
                if (sum == -1) System.out.println(sum);
            }) + " ns");
            System.out.println("ArrayList iterate: " + measure(() -> {
                long sum = 0; for (int x : a) sum += x;
                if (sum == -1) System.out.println(sum);
            }) + " ns");
            System.out.println("LinkedList iterate: " + measure(() -> {
                long sum = 0; for (int x : l) sum += x;
                if (sum == -1) System.out.println(sum);
            }) + " ns");
            System.out.println("ArrayList front insert/remove: " + measure(() -> {
                for (int i = 0; i < 100; i++) a.add(0, i);
                for (int i = 0; i < 100; i++) a.remove(0);
            }) + " ns");
            System.out.println("LinkedList front insert/remove: " + measure(() -> {
                for (int i = 0; i < 100; i++) l.add(0, i);
                for (int i = 0; i < 100; i++) l.remove(0);
            }) + " ns");
            System.out.println("ArrayList middle insert/remove: " + measure(() -> {
                int mid = a.size() / 2;
                for (int i = 0; i < 100; i++) a.add(mid, i);
                for (int i = 0; i < 100; i++) a.remove(mid);
            }) + " ns");
            System.out.println("LinkedList middle insert/remove: " + measure(() -> {
                int mid = l.size() / 2;
                for (int i = 0; i < 100; i++) l.add(mid, i);
                for (int i = 0; i < 100; i++) l.remove(mid);
            }) + " ns");
        }
        Deque<String> upNext = new ArrayDeque<>();
        upNext.addFirst("Urgent song");
        upNext.addLast("Later song");
        System.out.println("\nArrayDeque Up Next: " + upNext);
    }
}
