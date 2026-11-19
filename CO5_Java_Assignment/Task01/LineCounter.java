import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class LineCounter {
    public static long countBuffered(Path file) throws IOException {
        long lines = 0;
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            while (reader.readLine() != null) lines++;
        }
        return lines;
    }

    public static long countByteAtATime(Path file) throws IOException {
        long lines = 0;
        try (InputStream in = new FileInputStream(file.toFile())) {
            int value;
            while ((value = in.read()) != -1) if (value == '\n') lines++;
        }
        return lines;
    }

    private static void createFile(Path path, long bytes) throws IOException {
        byte[] block = new byte[8192];
        java.util.Arrays.fill(block, (byte) 'x');
        block[block.length - 1] = (byte) '\n';
        try (OutputStream out = Files.newOutputStream(path)) {
            long remaining = bytes;
            while (remaining > 0) {
                int n = (int) Math.min(block.length, remaining);
                out.write(block, 0, n);
                remaining -= n;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        long[] sizes = {1L << 20, 10L << 20, 50L << 20};
        for (long size : sizes) {
            Path f = Files.createTempFile("line-counter", ".txt");
            createFile(f, size);
            long start = System.nanoTime();
            long a = countByteAtATime(f);
            long byteMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            start = System.nanoTime();
            long b = countBuffered(f);
            long bufferedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            System.out.printf("%d MiB: byte=%d lines/%d ms, buffered=%d lines/%d ms%n",
                    size / (1L << 20), a, byteMs, b, bufferedMs);
            Files.deleteIfExists(f);
        }
    }
}
