package workertest;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Worker<T extends Worker.Work> {
    public interface Work {
        void work();
    }

    private static int CAPACITY = 20;
    private static int WEB_CALL_THRESHOLD = 5;

    private BlockingQueue<T> mQueue = new LinkedBlockingQueue<T>(CAPACITY);
    private ExecutorService mExecutor;

    private ExecutorService mWorkerExecutor;
    private AtomicBoolean mShouldWorkerStop;
    private AtomicInteger mWorksStarted;
    private AtomicInteger mWorksFinished;

    public enum State {
        INITIAL,
        OPERETIONAL,
        STOPPED
    }
    private State mState;

    public Worker() {
        mState = State.INITIAL;
        mShouldWorkerStop = new AtomicBoolean(false);
        mWorksStarted = new AtomicInteger(0);
        mWorksFinished = new AtomicInteger(0);
    }

    public void submit(T w) throws IllegalStateException {
        mQueue.add(w);
    }

    public void start() {
        stateTransition(State.OPERETIONAL);
        mShouldWorkerStop.set(false);

        mExecutor = Executors.newFixedThreadPool(WEB_CALL_THRESHOLD);
        mWorkerExecutor = Executors.newSingleThreadExecutor();
        mWorkerExecutor.submit(() -> {
            while (!mShouldWorkerStop.get()) {
                try {
                    final Optional<T> t = Optional.ofNullable(mQueue.poll(1000, TimeUnit.MILLISECONDS));
                    if (t.isPresent()) {
                        mExecutor.submit(() -> {
                            System.out.println("[+] New thread started: " + Thread.currentThread().getName());
                            mWorksStarted.incrementAndGet();
                            t.get().work();
                            mWorksFinished.incrementAndGet();
                        });
                        System.out.println("[+] Submited work to mExecutor.");
                    }
                } catch (InterruptedException e) {
                    System.out.println("[!] Polling from queue interrupted.");
                }
            }
            System.out.println("[+] Main Worker Thread stopped successfully!");
        });
    }

    public void stop() {
        System.out.println("[+] Trying to stop worker.");
        mShouldWorkerStop.set(true);
        mWorkerExecutor.shutdownNow();
        mExecutor.shutdown();
        try {
            mExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mExecutor.shutdownNow();
        stateTransition(State.STOPPED);
        System.out.println("Work completed: " + mWorksFinished.get() + "/" + mWorksStarted.get());
    }

    private void stateTransition(State newState) {
        System.out.println("[~] " + mState.toString() + " -> " + newState.toString());
        mState = newState;
    }

    public State getState() {
        return mState;
    }
}
