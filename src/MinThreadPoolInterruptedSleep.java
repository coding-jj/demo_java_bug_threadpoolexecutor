
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class MinThreadPoolInterruptedSleep implements Runnable {

    public static void main(final String[] args) {
        new Thread(new MinThreadPoolInterruptedSleep()).start();
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

        // final ExecutorService executor = Executors.newFixedThreadPool(2);
        final ExecutorService executor = new ThreadPoolExecutor(1,
                1, 30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(1),
                new ThreadPoolExecutor.CallerRunsPolicy());

        for (int i = 0; i < 1E8; i++) {
            executor.submit(() -> {
                try {
                    Thread.sleep(1000);
                    System.out.println("sub tread ran");
                } catch (final InterruptedException e) {
                    System.out.println("SUB THREAD INTERRUPTED");
                }
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
