Log: [~] INITIAL -> OPERETIONAL

Indicates state change.

Log: [+] New thread started: pool-1-thread-1

Work pulled from queue and started in a brand new thread.

Log:

[+] Sleep ended after: 101 msec. Sleep duration was 100 msec.

[+] Worker ended after: 1254 msec. Sleep duration was 100 msec.

Shows that sleep on main thread has the desired duration. Thus, Worker::start() is not a blocking function.

Log: [+] Conversion C2F: 4.0 --> 39.2 from pool-1-thread-4

Http response and the thread that did all the work

Log: [+] Work completed: 5/5

How much work was completed between while worker was OPERATIONAL

Log: [+] Active threads: 0

Shows that worker has 0 active threads after being stopped.

Log: [!] Polling from queue interrupted. Size is: 0

Blocking poll call was interrupted because a call to stop() has been made.

Log:

[+] Total work done: 1020

[!] Total work done should be 1020. 20 before rapid submission and 1000 after.

Shows total work done in main().