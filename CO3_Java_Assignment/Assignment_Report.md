# Programming in Java – Assignment 3 (CO3)

**Title:** Exception Handling and Multithreading  
**Student A:** [Name, Register Number, GitHub URL]  
**Student B:** [Name, Register Number, GitHub URL]  
**Java Version:** JDK 8 or later

> Working report template based on the supplied CO3 assignment. Both students must run every program and replace placeholders with their own test evidence, debugging observations and measurements.

---

## Q1 – Handling Failure in a Bank ATM Simulation

### Failure classification
| Failure point | Example | Type | Handling |
|---|---|---|---|
| Invalid numeric input | Letters where amount is expected | InputMismatchException (unchecked) | Specific catch |
| Negative amount | Amount below zero | IllegalArgumentException (unchecked) | Validation / catch |
| Insufficient balance | Withdrawal exceeds balance | User-defined InsufficientFundsException (unchecked in sample) | Specific catch |
| Missing card file | File absent | FileNotFoundException (checked) | Specific catch |
| Unexpected runtime fault | Invalid internal state | RuntimeException (unchecked) | Final broader catch |

Relevant hierarchy: `Throwable` → `Exception` → `RuntimeException` → unchecked exceptions. Checked file exceptions extend `Exception` but not `RuntimeException`. `Error` is a separate branch under `Throwable` and normally is not caught by application code.

Catch blocks must be ordered from most specific to most general; otherwise a broad catch makes narrower catches unreachable. `finally` executes when control leaves the try/catch in normal and exceptional cases, and is used here to print transaction completion. It is not a guarantee against JVM termination or process failure.

### Joint conclusion
Catching every failure with one `catch (Exception e)` can be acceptable at a top-level boundary to prevent an application from crashing and to log unexpected errors. It loses precise recovery, clear user messages and diagnostic classification if used as the only handling strategy.

### Alternative
Pre-validation using `Scanner.hasNextInt()` avoids exceptions for expected invalid input and can return explicit error codes. It requires checks at each input boundary. Exception construction/capture is more expensive than normal control flow because stack traces are captured; both approaches use O(1) additional state per single error, while stack-trace storage depends on call depth.

**Student A evidence:** Draw the Throwable hierarchy and complete the failure classification table.  
**Student B evidence:** Add at least five input tests, record catch/finally execution and include one genuine stack trace.

---

## Q2 – User-Defined Exceptions and Propagation in an Online Examination Portal

### Exception design
`ExamException` is the checked base exception. `SubmissionStorageException` represents a storage-layer failure and preserves the original IOException as its cause. Checked exceptions force callers to handle or declare expected application failures.

Propagation: storage layer catches IOException → wraps it in SubmissionStorageException → service/controller receives ExamException → controller displays a safe message. The UI should not expose low-level disk details as its primary message, but retaining the cause supports debugging and logging.

### Complete Code
In `Task02/StorageDemo.java`, TODO 1 is completed using:
`throw new SubmissionStorageException("Could not store " + fileName, e);`

Expected output:
```
Saved answer01.pdf
Error: Could not store answer02.pdf
Cause: Disk quota exceeded
```
If the original cause is not passed, `getCause()` returns `null`; calling `getCause().getMessage()` would then cause a NullPointerException.

### Alternative
If the custom exceptions extend RuntimeException, method signatures do not need `throws`, and callers are not forced by the compiler to catch them. The application must still decide where to handle them. A stack trace uses memory proportional to captured stack depth; chaining retains the cause exception and its trace, increasing memory for the duration the chain is reachable.

**Student A evidence:** Exception class diagram and propagation path.  
**Student B evidence:** One scenario per exception, chained trace, cause-preserved vs cause-lost comparison and test table.

---

## Q3 – Resource Safety, Debugging and Robust Code

### Design
`Task03/SalesReport.java` demonstrates manual try/catch/finally closing and try-with-resources. A resource opened for each input file must be closed even when reading or writing fails. In try-with-resources, resources close automatically in reverse order of initialization. If the body throws and close also throws, the close exception is attached as a suppressed exception.

### Why manual close can leak
If an exception occurs before the explicit close statement, control can skip that statement unless close is placed correctly in finally. Nested resources make this error-prone. Try-with-resources reduces this risk but does not guarantee a complete report: input may be corrupt, permissions may fail, disk space may run out, and partial output may need transactional handling.

### Complexity
For f files containing a total of N input records, reading and aggregating is O(N) time plus output work. With one resource at a time, peak open file handles can be O(1); if all f files are opened together, it can be O(f). No closing can exhaust handles. Add your own failure injection and debugger evidence.

**Student A:** Breakpoint locations, failure input, open-handle observations and file-specific logs.  
**Student B:** Resource inventory, closing order and suppressed-exception explanation.

---

## Q4 – Thread Creation and the Thread Life Cycle

### Design
`Task04/ScannerStations.java` demonstrates four scanner stations implemented using both Thread and Runnable. The life cycle includes NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING and TERMINATED. Actual transitions depend on scheduling and synchronization.

Calling `start()` asks the JVM to schedule a new thread; calling `run()` directly is an ordinary method call on the current thread. `join()` makes the calling thread wait for the target thread to finish. `isAlive()` checks whether a thread has started and has not terminated. `sleep()` pauses the current thread for a time but does not release locks it holds.

### ExecutorService alternative
A fixed pool reuses p worker threads for n tasks, avoiding one new thread per task. With balanced CPU-bound work, idealized processing time can approach O(n/p) task-work units, but real performance depends on task cost, I/O, scheduling and contention. Thread stacks and worker state use O(p) thread resources.

**Student A:** Explain Thread vs Runnable, life-cycle diagram, run/start and join.  
**Student B:** Record ten finish-order runs, priority experiment and daemon experiment.

---

## Q5 – Race Conditions in a Cinema Seat-Booking System

### Race condition
The check-then-book sequence is a critical section. Without synchronization, two threads can both observe `booked[seat] == false` before either writes true, resulting in duplicate successful bookings or an incorrect count. Synchronization makes the operation atomic with respect to threads using the same monitor.

### Implementations
`Task05/SeatBooking.java` provides an unsynchronized demonstration and a synchronized booking method. Run the unsynchronized mode repeatedly to observe the race; the race is timing-dependent and may not reproduce on every run. The synchronized version should never count more successful bookings than the number of seats.

A synchronized instance method locks the current object (`this`). A synchronized block can use the same monitor around only the critical section, reducing the locked region if other work is moved outside safely. Static synchronized methods lock the Class object and are relevant when coordinating static shared state across instances.

### Alternative
An AtomicBoolean per seat can use compareAndSet(false, true) to atomically claim a seat. A ReentrantLock provides explicit lock/unlock and should use finally for unlock. A ConcurrentHashMap with putIfAbsent can represent claimed seat IDs. Each approach uses O(s) storage for s seats; booking is expected O(1) per seat, with contention affecting latency.

**Student A:** Construct interleaving and record duplicate-booking counts over ten trials.  
**Student B:** Measure 10,000 attempts and compare method vs block synchronization.

---

## Q6 – Deadlock and Inter-Thread Communication

### Part A: Producer–consumer
`Task06/TokenCounter.java` implements a bounded buffer of capacity five. `put()` waits while full; `take()` waits while empty. Both use while loops because a thread may wake spuriously or another thread may change the condition before it reacquires the monitor. `notifyAll()` wakes all waiters so each can re-check its condition. `wait()` releases the monitor while waiting; `sleep()` does not release a held monitor.

The total of tokens 1 through 20 is:
\[
rac{20(20+1)}{2}=210
\]
The expected program output is `Sum of tokens collected: 210 (expected 210)`.

Changing while to if is unsafe because a wake-up does not guarantee that the condition is now true.

### Part B: Deadlock
The four Coffman conditions are mutual exclusion, hold and wait, no preemption and circular wait. A consistent global lock order prevents circular wait. `tryLock()` with a timeout can allow retry or rollback, but requires careful handling of partial acquisition.

### Alternative
ArrayBlockingQueue of capacity five provides a tested bounded blocking queue; put/take are O(1) queue operations in the usual implementation model, with waiting under full/empty conditions. A sorted or ordered locking discipline can prevent deadlock. A timeout lock design must release any acquired first lock if the second acquisition fails.

**Student A:** Explain wait-loop, notifyAll, buffer bounds and sleep vs wait.  
**Student B:** Reproduce deadlock, identify four conditions and demonstrate lock ordering.

---

## Q7 – Design a Concurrent, Fault-Tolerant Processor from First Principles

### Requirements
Process 10,000 score files; independent files can be processed in parallel. Missing or corrupt files must be recorded as failures without stopping other tasks. A final summary must count successes and failures correctly.

### Design
`Task07/ResultProcessor.java` uses:
- ExecutorService with a fixed thread pool.
- Callable<Result> for each file.
- Future<Result> to collect completion and exceptions.
- ConcurrentLinkedQueue for successful and failed result records.
- Per-file exception handling so one bad file does not cancel the remaining work.

The main thread collects every Future before printing the summary. This makes the summary reflect all submitted tasks. The example includes sample valid, missing and corrupt file cases in `data/`.

### Measurements
Run with pool sizes 1, 2, 4 and 8. Record elapsed time, successful files, failed files and total processed. More threads may not help if the task is disk-bound, if storage is slow, or if scheduling/coordination overhead dominates. Use the measured data to choose a pool size.

### Alternatives
A parallel stream uses a shared ForkJoinPool by default and provides concise parallel processing, but per-file failure handling and pool control require care. One thread per file creates up to 10,000 threads, with large stack/resource overhead and scheduling cost. A bounded ExecutorService controls concurrency at O(p) worker threads plus O(n) task/result bookkeeping.

**Student A:** Class diagram, pool design, collection choice and failure policy.  
**Student B:** Corrupt/missing-file injection, pool-size measurements and count verification.

---

## Individual Contribution Record

| Question | Student A – Primary responsibility | Student B – Primary responsibility | Joint |
|---|---|---|---|
| Q1 | Exception design | Testing and tracing | ATM design |
| Q2 | Hierarchy and propagation | Scenarios and chaining | Portal conclusion |
| Q3 | Debugging and experiments | Resource safety and correctness | Report generator |
| Q4 | Thread design | Behavioural experiments | Scanner simulation |
| Q5 | Race reproduction | Synchronization comparison | Booking design |
| Q6 | Producer–consumer | Deadlock analysis | Wallet/token design |
| Q7 | Architecture | Implementation and measurement | Final defence |

## Final Submission Checklist
- [ ] Both students have their own repository named `CSA09-<RegisterNumber>-Assignment3`.
- [ ] Both students have independently run Q1–Q7.
- [ ] Student A and Student B sections are labelled in the report.
- [ ] Q1 has at least five test inputs and one genuine stack trace.
- [ ] Q3 has debugging evidence and resource failure tests.
- [ ] Q4 has ten finish-order runs and priority/daemon observations.
- [ ] Q5 has ten unsynchronized trials and synchronized validation.
- [ ] Q7 has results for pool sizes 1, 2, 4 and 8.
- [ ] Screenshots, logs, raw timing data and GitHub links are included.
