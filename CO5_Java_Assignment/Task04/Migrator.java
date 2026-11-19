import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.Connection;
import java.util.*;

public class Migrator {
    private static final int BATCH_SIZE = 1000;

    public static void migrate(Path input, Path rejectFile) throws Exception {
        List<String[]> batch = new ArrayList<String[]>(BATCH_SIZE);
        List<String> rejects = new ArrayList<String>();
        long lineNo = 0;
        int loaded = 0;

        try (Connection connection = DbConfig.connect();
             BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                try {
                    String[] fields = line.split(",", -1);
                    if (fields.length != 5)
                        throw new IllegalArgumentException("Expected 5 fields");
                    if (fields[0].trim().isEmpty() || fields[1].trim().isEmpty() ||
                            fields[2].trim().isEmpty())
                        throw new IllegalArgumentException("Required field is empty");
                    int route = Integer.parseInt(fields[3]);
                    java.math.BigDecimal amount = new java.math.BigDecimal(fields[4]);
                    if (route <= 0) throw new IllegalArgumentException("Route must be positive");
                    if (amount.signum() < 0) throw new IllegalArgumentException("Negative amount");
                    batch.add(fields);
                    if (batch.size() == BATCH_SIZE) {
                        loaded += PassLoader.loadAll(connection, batch);
                        batch.clear();
                    }
                } catch (RuntimeException ex) {
                    rejects.add("Line " + lineNo + ": " + ex.getMessage() + " | " + line);
                }
            }
            if (!batch.isEmpty()) loaded += PassLoader.loadAll(connection, batch);
        }

        Files.write(rejectFile, rejects, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("Loaded rows: " + loaded);
        System.out.println("Rejected rows: " + rejects.size());
        System.out.println("Reject report: " + rejectFile.toAbsolutePath());
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java Migrator <input.csv> <reject-report.txt>");
            return;
        }
        migrate(Paths.get(args[0]), Paths.get(args[1]));
    }
}
