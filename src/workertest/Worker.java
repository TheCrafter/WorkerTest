package workertest;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Worker<T extends Worker.Work> {
    public interface Work {
        void work();
    }

    private static int CAPACITY = 20;
    private static int WEB_CALL_THRESHOLD = 5;

    public BlockingQueue<T> mQueue = new LinkedBlockingQueue<T>(CAPACITY);
    private ExecutorService mExecutor;

    private ExecutorService mWorkerExecutor;
    private AtomicBoolean mShouldWorkerStop;

    public enum State {
        INITIAL,
        OPERETIONAL,
        STOPPED
    }
    private State mState;

    public Worker() {
        mState = State.INITIAL;
        mShouldWorkerStop = new AtomicBoolean(false);
    }

    public void submit(T w) throws IllegalStateException {
        mQueue.add(w);
    }

    public void start() {
        mState = State.OPERETIONAL;
        mShouldWorkerStop.set(false);

        mExecutor = Executors.newFixedThreadPool(WEB_CALL_THRESHOLD);
        mWorkerExecutor = Executors.newSingleThreadExecutor();
        mWorkerExecutor.submit(() -> {
            while (!mShouldWorkerStop.get()) {
                mExecutor.submit(() -> {
                    try {
                        Optional<T> t = Optional.ofNullable(mQueue.poll(1000, TimeUnit.MILLISECONDS));
                        if (t.isPresent()) {
                            t.get().work();
                        }
                        
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
            System.out.println("Yay. I stopped!");
        });
    }

    public void stop() {
        mState = State.STOPPED;
        mShouldWorkerStop.set(true);
        mExecutor.shutdown();
        mExecutor.shutdownNow();
        mWorkerExecutor.shutdownNow();
    }

    public State getState() {
        return mState;
    }
}
