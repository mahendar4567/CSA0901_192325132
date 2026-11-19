import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class PassStore {
    private final Path file;

    public PassStore(Path file) { this.file = file; }

    public void add(String passId, String regNo, String name, int route, double amount)
            throws IOException {
        validate(passId, regNo, name, route, amount);
        String[] fields = {passId, regNo, name, String.valueOf(route), String.valueOf(amount)};
        for (String field : fields) {
            if (field.contains(",") || field.contains("\n") || field.contains("\r"))
                throw new IllegalArgumentException("Simple CSV fields cannot contain comma/newline");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(String.join(",", fields));
            writer.newLine();
        }
    }

    /** Returns five fields for the pass ID, or null when not found. */
    public String[] find(String passId) throws IOException {
        if (!Files.exists(file)) return null;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            long lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String[] fields = parse(line, lineNo);
                if (fields != null && fields[0].equals(passId)) return fields;
            }
        }
        return null;
    }

    public List<String[]> listByRoute(int route) throws IOException {
        List<String[]> result = new ArrayList<String[]>();
        if (!Files.exists(file)) return result;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            long lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String[] fields = parse(line, lineNo);
                if (fields != null && Integer.parseInt(fields[3]) == route) result.add(fields);
            }
        }
        return result;
    }

    private String[] parse(String line, long lineNo) {
        if (line.trim().isEmpty()) {
            System.err.println("Skipping damaged line " + lineNo + ": empty line");
            return null;
        }
        String[] f = line.split(",", -1);
        if (f.length != 5) {
            System.err.println("Skipping damaged line " + lineNo + ": expected 5 fields");
            return null;
        }
        try {
            validate(f[0], f[1], f[2], Integer.parseInt(f[3]), Double.parseDouble(f[4]));
            return f;
        } catch (RuntimeException ex) {
            System.err.println("Skipping damaged line " + lineNo + ": " + ex.getMessage());
            return null;
        }
    }

    private static void validate(String id, String regNo, String name, int route, double amount) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("missing pass ID");
        if (regNo == null || regNo.trim().isEmpty()) throw new IllegalArgumentException("missing register number");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("missing name");
        if (route <= 0) throw new IllegalArgumentException("route must be positive");
        if (!Double.isFinite(amount) || amount < 0) throw new IllegalArgumentException("invalid amount");
    }

    public static void main(String[] args) throws Exception {
        Path f = Files.createTempFile("passes", ".csv");
        PassStore store = new PassStore(f);
        store.add("P1001", "192311001", "Arun", 12, 4500);
        Files.writeString(f, "P1002,DAMAGED LINE\n", StandardOpenOption.APPEND);
        store.add("P1003", "192311003", "Divya", 7, 3800);
        System.out.println(java.util.Arrays.toString(store.find("P1003")));
        System.out.println(java.util.Arrays.toString(store.find("P1002")));
        Files.deleteIfExists(f);
    }
}
