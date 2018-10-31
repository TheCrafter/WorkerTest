Implementation for a Java assessment exercise.

Implement a generic Worker mechanism that can be used by an application to perform various types of processing.

A Worker is an autonomous processing engine that uses its own processing resources (e.g. threads) to do its work. Each Worker should be able to process its own types of Work. Workers should perform their processing in an asynchronous way, NOT delaying in any way the hosting application's threads. Workers should operate in three states, INITIAL, OPERATIONAL and STOPPED as explained below:

- INITIAL: State right after Worker instantiation, NOT for processing Work.
- OPERATIONAL: State in which, Workers normally accept and process Work.
- STOPPED: State indicating possible imminent shutdown, NOT for processing Work.

The hosting application must be able to interact with Worker implementations via well defined interfaces. Specifically and at a minimum:

- An interface to control the life cycle of a Worker and to submit Work units.
- An interface or abstract class as the root of the Work unit hierarchy.

The interfaces should support Java Generics, to provide compile-time type safety to the hosting application's developers. The interfaces should be possible to be used in the following ways:
- Worker<SomeWork> worker = new SomeWorkWorker<SomeWork>();
- Attempting to submit SomeOtherWork to a Worker<SomeWork> should be reported as a compile-time error.

The following state transitions should be supported: <NOT EXISTS> --> INITIAL --> OPERATIONAL <--> STOPPED

Workers should use their own threads and thread pools to perform their processing, according to the processing needs of each implementation. The only restrictions imposed regarding threads and thread pools are the following:

- When in the INITIAL or STOPPED states, a Worker should NOT have ANY threads alive.
- Transitioning into the OPERATIONAL state, a Worker should create and initialize its threads or thread pools.
- Transitioning from the OPERATIONAL to the STOPPED state, a Worker should ensure that when the hosting application's thread returns from the transition-triggering method, processing of ALL Work units being actively processed by the Worker's threads HAS FINISHED.

Workers should also make use of queuing mechanisms to both be able to accept Work units for processing even when all their processing resources are busy, and not impose any delays to the hosting application's threads. To prevent out-of-memory conditions, Workers should indicate their inability to further queue additional Work units, when their queues already have "many" items, by throwing an appropriate exception to the calling hosting application.


IMPLEMENTATION

To demonstrate use of the mechanism as well as to prove its correct operation, an example Worker should be implemented to use the TempConvert Web Service (http://www.w3schools.com/xml/tempconvert.asmx) to convert temperatures between Celsius and Fahrenheit. The temperature as well as the conversion to perform should be parameters of the corresponding Work unit. The Worker should perform no more than 5 concurrent calls to the Web Service, but also should not be limited to one, and it should be able to queue up to 20 Work units.

The demonstrating program should create an instance of the Worker, submit a few Work units with various combinations of temperatures and conversions (demonstrating normal operation) and then attempt to saturate the queue by rapid submission of many Work units. It should then demonstrate state transitions and exit.

You are allowed to use any version of the JDK, starting from version 6.
Do not use any libraries other than what Java provides in the JDK.