# Programming in Java – Assignment 2 (CO2)

**Title:** Strings and the Java Collection Framework  
**Student Name:** [Enter your name]  
**Register Number:** [Enter your register number]  
**GitHub Link:** [Paste repository link]  
**Java Version:** JDK 8 or later

> Use this as a structured working report. Execute the programs on your own system, add actual timing data and screenshots, and revise the conclusions to match your observations.

---

## Q1 – Building Text Under Load: String, StringBuilder or StringBuffer?

### Prediction
`String` is immutable. Repeated concatenation creates new String objects and copies the existing characters again. For a ticket with many fields, repeated `+` inside a loop can therefore perform much more copying than a mutable builder. `StringBuilder` is normally the suitable per-ticket choice in a method-local operation. `StringBuffer` has synchronized methods and is useful when the same mutable buffer must be shared by multiple threads, although a shared log is often better protected by a clear higher-level synchronization strategy.

### Workloads and implementation
`Task01/StringBuildBenchmark.java` runs three workload sizes and compares concatenation, StringBuilder and StringBuffer. It uses `System.nanoTime()` and repeated rounds. Run it on your computer and retain the printed timings. Timing varies with JVM warm-up, garbage collection, CPU load and JDK version; no universal millisecond result is claimed here.

### Complexity
Let n be the number of appended fields and assume their total output length grows proportionally with n. Repeated immutable concatenation can copy prefixes repeatedly, giving O(n²) total character copying in the simple model. StringBuilder appends amortized O(1) per append and produces the final text in O(n) total time. Both builders need O(n) output storage; repeated concatenation can create additional temporary strings and transient memory pressure.

### Decision
Use a local StringBuilder for each ticket. For concurrent log writes, use a dedicated synchronized logger or a thread-safe logging framework; if choosing only between these two mutable classes for one shared buffer, StringBuffer synchronizes its methods. Synchronization of individual calls does not automatically make a multi-call record append atomic.

### Alternative
`String.join()` or `StringJoiner` can be clean for joining an existing collection of fields with a delimiter. `String.format()` is convenient for formatted output but may have more formatting overhead. They do not remove the need to consider allocation and workload size.

**Measured evidence to add:** Copy the output from Task01, include at least three runs, and compare the average for each method.

---

## Q2 – When Is ArrayList an Appropriate Engineering Choice?

### Experimental design
`Task02/ListBenchmark.java` compares ArrayList and LinkedList at three list sizes for end insertion, front insertion/removal, middle insertion/removal, indexed access and full iteration. The program reports elapsed nanoseconds. Repeat runs and use averages; small timings are noisy.

### Analysis
| Operation | ArrayList | LinkedList | ArrayDeque |
|---|---:|---:|---:|
| Add at end | Amortized O(1) | O(1) | Amortized O(1) |
| Insert at front | O(n) | O(1) with list head | O(1) |
| Insert in middle | O(n) | O(n) to reach position, O(1) link once there | Not an indexed-list operation |
| Remove front | O(n) | O(1) | O(1) |
| Get k-th | O(1) | O(n) | No indexed access |
| Iterate all | O(n) | O(n) | O(n) |

ArrayList stores references in a resizable array, so indexed access is fast and storage overhead per element is relatively small. LinkedList stores each element in a node with links, requiring extra memory. LinkedList is not automatically faster for middle operations because finding the position is O(n).

### Recommendation
ArrayList is reasonable when the application frequently reads by index, iterates, and appends, while front/middle insertions are relatively uncommon. For an Up Next queue with frequent operations at both ends, ArrayDeque is a better fit. It does not support arbitrary indexed access.

### Alternative
A plain array with manual shifting has O(1) indexed access, O(1) append while capacity remains, and O(n) insertion/removal when shifting is needed. Resizing may require O(n) copying.

**Measured evidence to add:** Paste your benchmark output and state the tested sizes and machine/JDK.

---

## Q3 – Engineer the hashCode–equals Contract

### Contract
If two objects are equal according to `equals()`, they must return the same `hashCode()`. Unequal objects may share a hash code, but that can increase collisions. HashSet uses the hash to find a bucket and then uses equality checks to distinguish elements.

### Tests
1. Repeated scans of the same registration number.
2. Deliberately create different Student objects with the same registration number.
3. Check an additional case, such as two different registration numbers with the same name.

The completed class in `Task03/Attendance.java` makes `regNo` final and uses it in both `equals()` and `hashCode()`. The expected unique count for the supplied data is 2. If hashCode is not overridden, equal objects can be placed in different hash buckets, so duplicate logical students may remain.

### Complexity and alternatives
HashSet add/contains are O(1) average and O(n) worst case; storage is O(n). TreeSet with a comparator is O(log n) per operation and stores O(n) elements. HashMap keyed by registration number is also O(1) average and makes the uniqueness key explicit. Mutating a field used by hashCode after insertion can make an object difficult to find/remove, so equality-key fields should be immutable.

---

## Q4 – Choosing a Map for a Pharmacy Inventory

### Design
`Task04/PharmacyMaps.java` stores medicine code and stock in HashMap, TreeMap and LinkedHashMap and prints the iteration order.

| Map | put/get average | Iteration | Natural order |
|---|---:|---:|---|
| HashMap | O(1) | O(n) | No guaranteed order |
| TreeMap | O(log n) | O(n) | Sorted by key |
| LinkedHashMap | O(1) average | O(n) | Insertion order |

TreeMap naturally creates the code-order report. LinkedHashMap naturally creates the insertion-order report. HashMap requires copying and sorting keys when sorted output is needed. Hashtable is a legacy synchronized map; modern code usually uses HashMap or an explicit concurrent collection when concurrency is required.

### Alternative: sorted array
Binary search lookup is O(log n). Inserting while preserving order is O(n) because elements may need shifting. Producing the sorted report is O(n). The array can have lower per-entry overhead but is less convenient for frequent updates.

**Measurements:** Run the program at three inventory sizes and add your own lookup and report timings. Define an acceptable lookup response time for your intended application instead of assuming one.

---

## Q5 – Derive a Generic Ranking Component

### Design
The `Leaderboard<T>` class accepts a `Comparator<? super T>`, so it can rank different types using an externally supplied rule. `ranking()` returns a sorted copy and does not modify the internal insertion list.

For teams, the comparator sorts points descending, goal difference descending, and team name ascending. The comparator chain uses `comparingInt(...).reversed()` and `thenComparing(...)`. Bisons is above Panthers because both have 9 points and goal difference 4, so the final alphabetical comparison places Bisons first.

### Correctness
A comparator should define a consistent ordering. Avoid subtraction such as `b.points - a.points`, because integer overflow can produce incorrect results. Use comparison methods instead. Test ties at every comparison level and deliberately faulty comparators.

### Complexity and alternatives
Sorting n items takes O(n log n) time. The copied list uses O(n) extra references. If Team implements Comparable, there is one natural ordering for Team; Comparator is more reusable when the same type needs multiple ranking rules. Three stable sorts can also implement multi-key ordering when performed from least-significant key to most-significant key, but they repeat sorting work. A comparator chain is clearer and performs one sort.

`List<? extends T>` is useful when a method consumes a list of a subtype of T. The `? super T` comparator allows a comparator of T or one of its supertypes.

---

## Q6 – Priority, Order and Fairness in a Queue

### Design
A plain FIFO queue cannot select the most severe patient first. A PriorityQueue orders patients by severity ascending (1 first), then by arrival counter ascending. The counter ensures first-arrived order for equal severities.

`Task06/Triage.java` implements this rule. The expected treatment order for the supplied sample is:
`Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)`

Printing a PriorityQueue directly does not guarantee priority iteration order. Repeatedly calling `poll()` produces the actual service order.

### Complexity and fairness
PriorityQueue offer and poll are O(log n); peek is O(1); storage is O(n). A sorted LinkedList has O(n) insertion and O(1) removal from the head. Five FIFO queues can offer O(1) admission and O(1) next-patient selection when checking the five severity queues, but require a fixed priority policy.

Strict priority can starve severity-5 patients if critical arrivals continue. A fair rule could promote a patient's effective priority after a documented waiting-time threshold. The policy must preserve clinical safety and be approved by the hospital; the code example demonstrates ordering, not a medical triage policy.

### Scale
Five thousand arrivals per day averages about 0.058 arrivals per second across a full day, but real arrivals are uneven and peak load matters. The data structure alone cannot establish production readiness; concurrency, persistence, failover, privacy and clinical workflow need separate validation.

---

## Q7 – Diagnose, Redesign and Validate

### Selected limitation
The sample redesign focuses on repeated linear lookup in a list of records. A list scan is O(n) per lookup and can become expensive when many lookups are made.

### Objective
Keep the ordered list for display while adding a HashMap index keyed by employee/record ID. The target is O(1) average lookup while retaining O(n) ordered traversal.

### Revised design
`Task07/RedesignDemo.java` compares repeated linear search with indexed HashMap lookup using the same records and query IDs. The index uses O(n) additional space. Insertions and deletions must update both structures to avoid inconsistent data.

### Prediction and validation
The map-backed lookup should require fewer comparisons as n grows, at the cost of extra memory and index maintenance. Run the same test sizes repeatedly and record measured values. The benchmark output is hardware/JVM dependent; use your own results in the final conclusion.

### Trade-offs
A list remains simple and efficient for sequential traversal and small data sets. The indexed design is preferable when lookups are frequent and IDs are unique. If ordering by ID is also required, a TreeMap may be more suitable, with O(log n) lookup and sorted iteration.

---

## Final Submission Checklist
- [ ] Q1–Q7 Java files compile and execute.
- [ ] Each task has actual output and screenshots.
- [ ] Q1, Q2, Q4 and Q7 contain repeated measurements from your computer.
- [ ] Predictions are written before the measured results.
- [ ] Raw data, tables and graphs are included where relevant.
- [ ] Student name, register number and GitHub link are filled in.
- [ ] You can explain every design decision during viva.
