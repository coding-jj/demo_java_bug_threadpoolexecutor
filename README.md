# Demo Java Bug ThredPoolExecutor Demo

This is the demo code for the Stackoverflow question 
[Interrupting Thread with ThreadPoolExecutor and ArrayBlockingQueue not working](http://stackoverflow.com/questions/42475085/interrupting-thread-with-threadpoolexecutor-and-arrayblockingqueue-not-working). 

## The Problem

The demo code implements a shutdown hook. If you one of this applications from command line (not from eclipse) 
and press <kbd>CRTL</kbd>-<kbd>C</kbd>. The shutdown hook will interrupt the main running thread and wait up to 
10 Seconds for the main thread to finish. Depending on the time where you press <kbd>CRTL</kbd>-<kbd>C</kbd> the 
thread will be interrupted in the ThreadPool `.submit()` or the ThreadPool `.awaitTermination()` part. 

Normally the Thread will be interrupted and the ThreadPool `.shutdownNow()` will tear down the rest of the threads 
queued. No Problem so far.

If I start a runner thread with a `Thread.sleep()` with a `ThreadPoolExecutor` using a `ArrayBlockingQueue` or 
`LinkedBlockingDeque` the code will not interrupt as expected. Only one of the running sub thread is interrupted, 
but no the main thread. The program is running until the shutdown hook will exit after 10 seconds.

## Demo Code Minified

The Demo Code consists of 4 independent java files, each has all the code it needs and can run on it's own. 
It is the minified version of the problem.

#### Code Minified with Problem

- [MinThreadPoolInterruptedSleep](src/MinThreadPoolInterruptedSleep.java)

#### Code Minified with no Problem

- [MinFixedPoolInterruptedMath](src/MinFixedPoolInterruptedMath.java)
- [MinFixedPoolInterruptedSleep](src/MinFixedPoolInterruptedSleep.java)
- [MinThreadPoolInterruptedMath](src/MinThreadPoolInterruptedMath.java)

## Demo Code Long

The Demo Code consists of 4 independent java files, each has all the code it needs and can run on it's own. The 
application  can be run in Eclipse. I created a `ThreadInterupter` which is interrupting the main thread after 
15 seconds. This code gives some extra information und configuration variables.

#### Code Long with Problem

- [ThreadPoolInterruptedSleep](src/ThreadPoolInterruptedSleep.java)

#### Code Long with no Problem

- [FixedPoolInterruptedMath](src/FixedPoolInterruptedMath.java)
- [FixedPoolInterruptedSleep](src/FixedPoolInterruptedSleep.java)
- [ThreadPoolInterruptedMath](src/ThreadPoolInterruptedMath.java)

## Solution

[Answer by Calculator on StackOverflow](http://stackoverflow.com/a/42511433/5330578)
with Reference to the excellent [Answer by aioobe](http://stackoverflow.com/a/3976377)


The original Runner Thread: 

        try {
            Thread.sleep(1000);
            System.out.println("sub tread ran");
        } catch (final InterruptedException e) {
            System.out.println("SUB THREAD INTERRUPTED");
        }

The modified Runner Thread:

        try {
            Thread.sleep(1000);
            System.out.println("sub tread ran");
        } catch (final InterruptedException e) {
            System.out.println("SUB THREAD INTERRUPTED");
            Thread.currentThread().interrupt();
        }

### Complete Solution

- [MinThreadPoolInterruptedSleepByCalculator](src/MinThreadPoolInterruptedSleepByCalculator.java)
- [ThreadPoolInterruptedSleepByCalculator](src/ThreadPoolInterruptedSleepByCalculator.java)

## How to Run and Test

- Check out GIT or download single java file.
- Build with Eclipse or javac
- Open command line and change directory to the java bin directory
- Run with `java ThreadPoolInterruptedSleep`
- Interrupt with <kbd>CRTL</kbd>-<kbd>C</kbd> or wait 15 seconds until `ThreadInterupter` strikes.

## Demo Video

The Demo Video is showing the interrupting process. First the failed interrupt from `ThreadPoolInterruptedSleep` and 
second the the successful interrupt from `ThreadPoolInterruptedMath`.

![Demo Video](video/DemoVideoThreadPoolExecutor.gif)
