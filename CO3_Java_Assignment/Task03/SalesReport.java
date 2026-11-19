import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SalesReport {
    private static void manualReport(List<Path> inputs, Path output) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = Files.newBufferedWriter(output);
            for (Path input : inputs) {
                BufferedReader reader = null;
                try {
                    reader = Files.newBufferedReader(input);
                    String line;
                    while ((line = reader.readLine()) != null) writer.write(line + System.lineSeparator());
                } finally {
                    if (reader != null) reader.close();
                }
            }
        } finally {
            if (writer != null) writer.close();
        }
    }

    private static void safeReport(List<Path> inputs, Path output) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            for (Path input : inputs) {
                try (BufferedReader reader = Files.newBufferedReader(input)) {
                    String line;
                    while ((line = reader.readLine()) != null) writer.write(line + System.lineSeparator());
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Path one = Paths.get("store1.txt");
        Path two = Paths.get("store2.txt");
        Files.write(one, Arrays.asList("Store 1: 1200", "Store 1: 900"));
        Files.write(two, Arrays.asList("Store 2: 1500"));
        List<Path> inputs = Arrays.asList(one, two);

        manualReport(inputs, Paths.get("manual-report.txt"));
        safeReport(inputs, Paths.get("safe-report.txt"));
        System.out.println("Manual report: " + Files.readAllLines(Paths.get("manual-report.txt")));
        System.out.println("Safe report: " + Files.readAllLines(Paths.get("safe-report.txt")));

        Files.deleteIfExists(one);
        Files.deleteIfExists(two);
    }
}
