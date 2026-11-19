# Q7 – Concurrent Fault-Tolerant Result Processor

Compile: `javac ResultProcessor.java`  
Run: `java ResultProcessor data` or `java ResultProcessor data 8`

Processes `.txt` files using a fixed thread pool. Each file produces a success or failure record; one bad file does not stop other files. Test pool sizes 1, 2, 4 and 8.
