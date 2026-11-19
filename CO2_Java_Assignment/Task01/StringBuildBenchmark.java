public class StringBuildBenchmark {
    private static volatile int sink;

    private static String usingString(int tickets, int fields) {
        String last = "";
        for (int t = 0; t < tickets; t++) {
            String s = "";
            for (int f = 0; f < fields; f++) s += "FIELD" + f + "|";
            last = s;
        }
        return last;
    }

    private static String usingBuilder(int tickets, int fields) {
        String last = "";
        for (int t = 0; t < tickets; t++) {
            StringBuilder s = new StringBuilder();
            for (int f = 0; f < fields; f++) s.append("FIELD").append(f).append('|');
            last = s.toString();
        }
        return last;
    }

    private static String usingBuffer(int tickets, int fields) {
        String last = "";
        for (int t = 0; t < tickets; t++) {
            StringBuffer s = new StringBuffer();
            for (int f = 0; f < fields; f++) s.append("FIELD").append(f).append('|');
            last = s.toString();
        }
        return last;
    }

    private static long time(int mode, int tickets, int fields) {
        long start = System.nanoTime();
        String result = mode == 0 ? usingString(tickets, fields)
                : mode == 1 ? usingBuilder(tickets, fields)
                : usingBuffer(tickets, fields);
        long elapsed = System.nanoTime() - start;
        sink = result.length();
        return elapsed;
    }

    public static void main(String[] args) {
        int[][] workloads = {{1, 5}, {1000, 50}, {10000, 50}};
        String[] names = {"String +", "StringBuilder", "StringBuffer"};
        for (int[] w : workloads) {
            System.out.println("\nTickets=" + w[0] + ", fields=" + w[1]);
            for (int mode = 0; mode < 3; mode++) {
                long total = 0;
                for (int r = 0; r < 4; r++) total += time(mode, w[0], w[1]);
                System.out.println(names[mode] + " average ns: " + (total / 4));
            }
        }
    }
}
