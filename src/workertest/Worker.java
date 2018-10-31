package workertest;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Worker<T extends Worker.Work> {
    public interface Work {
        void work();
    }

    // Config
    private static int CAPACITY = 20;
    private static int WEB_CALL_THRESHOLD = 5;

    // Executor to submit work
    private BlockingQueue<T> mQueue = new LinkedBlockingQueue<T>(CAPACITY);
    private ExecutorService mExecutor;

    // Single thread executor for worker's operating thread
    private ExecutorService mWorkerExecutor;
    private AtomicBoolean mShouldWorkerStop;

    // Thread safe counters
    private AtomicInteger mWorksStarted;
    private AtomicInteger mWorksFinished;
    private AtomicInteger mTotalWorkDone;

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
        mTotalWorkDone = new AtomicInteger(0);
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
                // FixedThreadPool indeed has a threshold of its own but it allows work to be submitted even when the threshold is reached.
                // Extra work will remain in a queue until there is an opening.
                // This is a workaround to not allow extra threads to be submitted to our executor.
                // Without this, all items from mQueue will be immediately submitted. This may be okay in the real world but it doesn't help in showcasing
                // this exercise's results :)
                if (mWorksStarted.get() - mWorksFinished.get() >= WEB_CALL_THRESHOLD) {
                    continue;
                }

                try {
                    final Optional<T> t = Optional.ofNullable(mQueue.poll(1000, TimeUnit.MILLISECONDS));
                    if (t.isPresent()) {
                        mWorksStarted.incrementAndGet();
                        mExecutor.submit(() -> {
                            System.out.println("[+] New thread started: " + Thread.currentThread().getName());
                            t.get().work();
                            mWorksFinished.incrementAndGet();
                        });
                    }
                } catch (InterruptedException e) {
                    System.out.println("[!] Polling from queue interrupted. Size is: " + mQueue.size());
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
        System.out.println("[+] Work completed: " + mWorksFinished.get() + "/" + mWorksStarted.get());
        if (mExecutor instanceof ThreadPoolExecutor) {
            System.out.println("[+] Active threads: " + ((ThreadPoolExecutor) mExecutor).getActiveCount());
        }

        mTotalWorkDone.set(mTotalWorkDone.get() + mWorksFinished.get());
        mWorksStarted.set(0);
        mWorksFinished.set(0);
    }

    private void stateTransition(State newState) {
        System.out.println("[~] " + mState.toString() + " -> " + newState.toString());
        mState = newState;
    }

    public State getState() {
        return mState;
    }

    public int getTotalWorkDone() {
        return mTotalWorkDone.get();
    }
}
