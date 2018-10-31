package workertest;

import java.util.Date;
import java.util.Optional;
import java.util.Random;

import workertest.HttpClient.OnTempratureConverted;
import workertest.Main.HttpWork.Type;

public class Main {

    public static class SomeOtherWork implements Worker.Work {
        @Override
        public void work() {
            System.out.println("[!] Other work.");
        }
    }

    public static class HttpWork implements Worker.Work {
        public enum Type {
            C2F,
            F2C
        }
        private Type type;
        private Double val;

        public HttpWork(Type t, Double v) {
            this.type = t;
            this.val = v;
        }

        @FunctionalInterface
        interface HttpCallback {
            void httpCb(double c, OnTempratureConverted tempCb);
        }

        @Override
        public void work() {
            HttpClient http = new HttpClient();
            HttpCallback cb = type == Type.C2F ? http::celsiusToFahrenheit : http::fahrenheitToCelsius;
            cb.httpCb(val, (Optional<Double> t) -> {
                System.out.println(
                        t.isPresent() ?
                        "[+] Conversion " + type.toString() + ": " + val + " --> " + t.get().toString() + " from " + Thread.currentThread().getName():
                        "[-] No conversion Possible");
                });
        }
    }

    public static void main(String[] args) {
        Worker<HttpWork> worker = new Worker<>();
        try {
            worker.submit(new HttpWork(Type.C2F, 1.0));
            worker.submit(new HttpWork(Type.C2F, 2.0));
            worker.submit(new HttpWork(Type.C2F, 3.0));
            worker.submit(new HttpWork(Type.C2F, 4.0));
            worker.submit(new HttpWork(Type.C2F, 5.0));
            worker.submit(new HttpWork(Type.C2F, 6.0));
            worker.submit(new HttpWork(Type.C2F, 7.0));
            worker.submit(new HttpWork(Type.C2F, 8.0));
            worker.submit(new HttpWork(Type.C2F, 9.0));
            worker.submit(new HttpWork(Type.C2F, 10.0));
            worker.submit(new HttpWork(Type.C2F, 11.0));
            worker.submit(new HttpWork(Type.C2F, 12.0));
            worker.submit(new HttpWork(Type.C2F, 13.0));
            worker.submit(new HttpWork(Type.C2F, 14.0));
            worker.submit(new HttpWork(Type.C2F, 15.0));
            worker.submit(new HttpWork(Type.C2F, 16.0));
            worker.submit(new HttpWork(Type.C2F, 17.0));
            worker.submit(new HttpWork(Type.C2F, 18.0));
            worker.submit(new HttpWork(Type.C2F, 19.0));
            worker.submit(new HttpWork(Type.C2F, 20.0));

            // Compile error if generic constraint is not satisfied
            //worker.submit(new SomeOtherWork());
        } catch (IllegalStateException e) {
            System.out.println("[!] Shouldn't have a problem here: " + e.toString());
        }

        try {
            worker.submit(new HttpWork(Type.C2F, 99.0));
        } catch (IllegalStateException e) {
            System.out.println("[+] Cannot add more work due to capacity restrictions!");
        }

        doWork(worker, 100);
        doWork(worker, 100);
        doWork(worker, 100);
        doWork(worker, 100);
        System.out.println("------------");
        System.out.println("[+] No more work here probably.");
        doWork(worker, 100);

        // Rapid submission
        Random r = new Random();
        for (int i = 0; i < 1000; ++i) {
            HttpWork.Type t;
            double min, max;
            if (r.nextBoolean()) {
                t = Type.C2F;
                min = -17.8;
                max = 40;
            } else {
                t = Type.F2C;
                min = 0;
                max = 104;
            }

            try {
                worker.submit(new HttpWork(t, min + (max - min) * r.nextDouble()));
            } catch (IllegalStateException e) {
                // Start doing work when queue if full at some point.
                System.out.println("[!] Queue if full. Retrying after 1 sec.");
                --i; // Retry on next loop because queue was full
                doWork(worker, 1000);
            }
        }
        doWork(worker, 5000);
        System.out.println("[+] Total work done: " + worker.getTotalWorkDone());
        System.out.println("[!] Total work done should be 1020. 20 before rapid submission and 1000 after.");
    }

    public static void doWork(Worker<?> w, long duration) {
        // Note that we need even a slight delay before stopping the worker.
        // Without that delay the worker won't even have enough time to spawn any threads.
        Long started = new Date().getTime();
        System.out.println("[+] Worker started at: " + started);
        w.start();
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Prove start() is non blocking and stop() is blocking.
        // Note that first call to start() should take 0.1 sec longer because Executor stuff are being initialized behind the scenes for the first time.
        System.out.println("[+] Sleep ended after: " + (new Date().getTime() - started) + " msec. Sleep duration was " + duration + " msec.");
        w.stop();
        System.out.println("[+] Worker ended after: " + (new Date().getTime() - started) + " msec. Sleep duration was " + duration + " msec.");
    }
}
