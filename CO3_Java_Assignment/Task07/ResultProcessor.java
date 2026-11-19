import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

class ProcessingResult {
    final String file;
    final boolean success;
    final int score;
    final String message;

    ProcessingResult(String file, boolean success, int score, String message) {
        this.file = file;
        this.success = success;
        this.score = score;
        this.message = message;
    }
}

class ScoreTask implements Callable<ProcessingResult> {
    private final Path file;
    ScoreTask(Path file) { this.file = file; }

    @Override
    public ProcessingResult call() {
        try {
            String value = new String(Files.readAllBytes(file)).trim();
            int score = Integer.parseInt(value);
            if (score < 0 || score > 100) throw new IllegalArgumentException("Score outside 0-100");
            return new ProcessingResult(file.getFileName().toString(), true, score, "OK");
        } catch (IOException | IllegalArgumentException e) {
            return new ProcessingResult(file.getFileName().toString(), false, 0,
                    e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}

public class ResultProcessor {
    public static void main(String[] args) throws Exception {
        Path directory = Paths.get(args.length > 0 ? args[0] : "data");
        int poolSize = args.length > 1 ? Integer.parseInt(args[1]) : 4;
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.txt")) {
            for (Path file : stream) files.add(file);
        }

        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        ConcurrentLinkedQueue<ProcessingResult> successes = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<ProcessingResult> failures = new ConcurrentLinkedQueue<>();
        List<Future<ProcessingResult>> futures = new ArrayList<>();
        long start = System.nanoTime();
        try {
            for (Path file : files) futures.add(pool.submit(new ScoreTask(file)));
            for (Future<ProcessingResult> future : futures) {
                ProcessingResult result = future.get();
                if (result.success) successes.add(result);
                else failures.add(result);
            }
        } finally {
            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.MINUTES);
        }

        long elapsed = System.nanoTime() - start;
        System.out.println("Successful files: " + successes.size());
        System.out.println("Failed files: " + failures.size());
        System.out.println("Total processed: " + (successes.size() + failures.size()));
        System.out.println("Pool size: " + poolSize);
        System.out.println("Elapsed ns: " + elapsed);
        for (ProcessingResult failure : failures)
            System.out.println("FAILED " + failure.file + " -> " + failure.message);
    }
}
