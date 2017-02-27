
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class FixedPoolInterruptedSleep implements Runnable {

    /**
     * Thread sleeps for the given Time.
     */
    public static class RunnerSleep implements Runnable {

        private final int mCount;
        private final long mTimeOutMs;

        /**
         * Thread sleeps for the given Time.
         *
         * @param pCount Thread Counter for display
         * @param pTimeOutMs Run Time in Milliseconds
         */
        public RunnerSleep(final int pCount, final long pTimeOutMs) {
            super();
            mCount = pCount;
            mTimeOutMs = pTimeOutMs;
        }

        @Override
        public void run() {

            try {
                Thread.sleep(mTimeOutMs);
            } catch (final InterruptedException e) {
                helperPrintStats("RunnerSleep " + mCount, "INTR");
                return;
            }

            helperPrintStats("RunnerSleep " + mCount + " fin", "");
        }
    }

    /**
     * Thread for Shutdown Hook
     */
    public static class ShutdownHookThread extends Thread {
        private final Thread mMainThread;

        public ShutdownHookThread(final Thread pMainThread) {
            super();
            mMainThread = pMainThread;
        }

        @Override
        public void run() {

            helperPrintStats("ShutdownHookThread", "START");

            mMainThread.interrupt();

            try {
                // Wait until Main Thread stops
                mMainThread.join(TimeUnit.SECONDS.toMillis(10));
            } catch (final InterruptedException e1) {
                // Ignore
            }

            helperPrintStats("ShutdownHookThread", "FIN");
        }
    }

    /**
     * Interrupter Thread.
     */
    public static class ThreadInterupter extends Thread {
        private final Thread mMainThread;
        private final long mTimeOutMs;

        /**
         * Interrupter Thread. Interrupt passed Thread after Time Out.
         *
         * @param pMainThread Thread to Interrupt
         * @param pTimeOutMs Time Out before Interrupter strikes
         */
        public ThreadInterupter(final Thread pMainThread, final long pTimeOutMs) {
            super();
            mMainThread = pMainThread;
            mTimeOutMs = pTimeOutMs;
        }

        @Override
        public void run() {
            super.run();

            try {
                Thread.sleep(mTimeOutMs);
                mMainThread.interrupt();

                helperPrintStats("ThreadInterupter", "INTR");

            } catch (final InterruptedException e) {
                // Ignore
            }
        }
    }

    /**
     * Start Time of Application
     */
    public static final long START_TIME = System.currentTimeMillis();

    /**
     * Print Runtime, Thread ID, Thread Status, Major Event, Thread Count.
     *
     * @param pThreadStatus Status Message of Thread
     * @param pEvent Event like 'INTR'
     */
    public static void helperPrintStats(final String pThreadStatus, final String pEvent) {

        System.out.format("%,7d ms   %5d ThrID    %-25s   %15s   %5d #Threads\n",
                System.currentTimeMillis() - START_TIME,
                Thread.currentThread().getId(),
                pThreadStatus,
                pEvent,
                Thread.activeCount());
    }

    /**
     * Main Method. Just start Main Thread.
     *
     * @param args Nothing to pass
     */
    public static void main(final String[] args) {
        new Thread(new FixedPoolInterruptedSleep()).start();
    }

    @Override
    public void run() {

        // Register Shutdown Hook
        final Thread tHookThread = new ShutdownHookThread(Thread.currentThread());
        Runtime.getRuntime().addShutdownHook(tHookThread);

        // Interrupter Thread will interrupt after 15 Seconds
        new ThreadInterupter(Thread.currentThread(), 15000).start();

        // Executor
        final ExecutorService ex = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 1000; i++) {

            final Runnable runner = new RunnerSleep(i, 1000);

            ex.submit(runner);
            helperPrintStats("main " + i + " submitted", "");

            // Is Thread interrupted break Submittion
            if (Thread.currentThread().isInterrupted()) {
                helperPrintStats("main " + i, "BREAK SUBMITTED");
                break;
            }
        }

        helperPrintStats("main", "fin submitted");

        // Friendly Executor Shutdown
        ex.shutdown();

        try {
            // Wait for Executor to Shutdown
            ex.awaitTermination(1, TimeUnit.HOURS);
        } catch (final InterruptedException e) {
            helperPrintStats("main", "INTR AWAIT");
        }

        // Force Executor Shutdown
        helperPrintStats("main", "shutdown now");
        ex.shutdownNow();

        // Wait until Executor really finishes
        try {
            ex.awaitTermination(1, TimeUnit.HOURS);
        } catch (final InterruptedException e) {
            // Ignore
        }

        helperPrintStats("main fin", "");
    }
}
