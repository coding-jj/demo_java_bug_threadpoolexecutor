
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class MinThreadPoolInterruptedMath implements Runnable {

    public static void main(final String[] args) {
        new Thread(new MinThreadPoolInterruptedMath()).start();
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

        final ExecutorService executor = new ThreadPoolExecutor(1,
                1, 30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(1),
                new ThreadPoolExecutor.CallerRunsPolicy());

        for (int i = 0; i < 1E8; i++) {
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
