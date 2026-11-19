import java.nio.file.*;
import java.util.Arrays;

public class PassStoreTest {
    public static void main(String[] args) throws Exception {
        Path file = Files.createTempFile("pass-test", ".csv");
        PassStore store = new PassStore(file);
        store.add("P1001", "192311001", "Arun", 12, 4500);
        Files.writeString(file, "\nP1002,DAMAGED LINE\nP1004,192311004,Invalid,4,abc\n",
                StandardOpenOption.APPEND);
        store.add("P1003", "192311003", "Divya", 12, 3800);

        System.out.println("Find P1003: " + Arrays.toString(store.find("P1003")));
        System.out.println("Find P9999: " + Arrays.toString(store.find("P9999")));
        System.out.println("Route 12 count: " + store.listByRoute(12).size());
        Files.deleteIfExists(file);
    }
}
