class ThreadStation extends Thread {
    private final String station;
    private final int scripts;

    ThreadStation(String station, int scripts) {
        this.station = station;
        this.scripts = scripts;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(100 + (long)(Math.random() * 300));
            System.out.println(station + " (Thread) scanned " + scripts + " scripts.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class RunnableStation implements Runnable {
    private final String station;
    private final int scripts;

    RunnableStation(String station, int scripts) {
        this.station = station;
        this.scripts = scripts;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(100 + (long)(Math.random() * 300));
            System.out.println(station + " (Runnable) scanned " + scripts + " scripts.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class ScannerStations {
    public static void main(String[] args) throws InterruptedException {
        Thread[] stations = {
            new ThreadStation("Station 1", 25),
            new Thread(new RunnableStation("Station 2", 30)),
            new ThreadStation("Station 3", 20),
            new Thread(new RunnableStation("Station 4", 25))
        };

        for (Thread t : stations) {
            t.start();
            System.out.println(t.getName() + " alive: " + t.isAlive());
        }
        for (Thread t : stations) t.join();
        System.out.println("All scanner stations completed.");
    }
}
