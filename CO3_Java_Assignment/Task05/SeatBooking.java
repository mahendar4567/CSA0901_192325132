import java.util.concurrent.CountDownLatch;

public class SeatBooking {
    private final boolean[] booked;
    int bookingsMade = 0;
    private final boolean synchronizedMode;

    SeatBooking(int seats, boolean synchronizedMode) {
        booked = new boolean[seats];
        this.synchronizedMode = synchronizedMode;
    }

    boolean book(int seat) {
        if (synchronizedMode) return bookSafely(seat);
        return bookUnsafely(seat);
    }

    private synchronized boolean bookSafely(int seat) {
        if (booked[seat]) return false;
        booked[seat] = true;
        bookingsMade++;
        return true;
    }

    private boolean bookUnsafely(int seat) {
        if (booked[seat]) return false;
        try { Thread.sleep(2); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        booked[seat] = true;
        bookingsMade++;
        return true;
    }

    public static void main(String[] args) throws InterruptedException {
        boolean race = args.length > 0 && args[0].equalsIgnoreCase("race");
        SeatBooking screen = new SeatBooking(10, !race);
        CountDownLatch ready = new CountDownLatch(100);
        CountDownLatch go = new CountDownLatch(1);
        Thread[] users = new Thread[100];

        for (int i = 0; i < users.length; i++) {
            final int seat = i % 10;
            users[i] = new Thread(() -> {
                ready.countDown();
                try { go.await(); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                screen.book(seat);
            });
            users[i].start();
        }
        ready.await();
        go.countDown();
        for (Thread t : users) t.join();
        System.out.println("Seats sold: " + screen.bookingsMade);
        System.out.println("Mode: " + (race ? "UNSYNCHRONIZED" : "SYNCHRONIZED"));
    }
}
