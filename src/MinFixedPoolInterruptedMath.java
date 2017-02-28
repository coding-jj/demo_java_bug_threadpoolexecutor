
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class MinFixedPoolInterruptedMath implements Runnable {

    public static void main(final String[] args) {
        new Thread(new MinFixedPoolInterruptedMath()).start();
    }

    @Override
    public void run() {

        // Register Shutdown Hook
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                mainThread.interrupt();
                mainThread.join(10000); // Chance for Main Thread to close
            } catch (final InterruptedException e) {
                // Ignore
            }
            System.out.println("SHUTDOWNHOOK FORCED EXIT");
        }));

        final ExecutorService executor = Executors.newFixedThreadPool(2);

        for (int i = 0; i < 1E4; i++) {
            executor.submit(() -> {
                final Random ran = new Random();
                for (int j = 0; j < 1E6; j++) {
                    Math.hypot(ran.nextDouble(), ran.nextDouble());

                    if (Thread.currentThread().isInterrupted()) {
                        System.out.println("SUB THREAD INTERRUPTED");
                        return;
                    }
                }
                System.out.println("sub tread ran");
            });

            if (Thread.currentThread().isInterrupted()) {
                System.out.println("MAIN SUBMIT INTERRUPTED");
                break;
            }
        }

        executor.shutdown();

        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (final InterruptedException e) {
            // Ignore
        }

        System.out.println("main force shutdown");
        executor.shutdownNow();

        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (final InterruptedException e) {
            // Ignore
        }

        System.out.println("main finished");
    }
}
