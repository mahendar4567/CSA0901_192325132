# B1 – Instrumented Bus-Pass File Store

Compile: `javac PassStore.java LineCounter.java PassStoreTest.java`

Run:
- `java PassStore` – demonstrates completed find TODO behaviour and damaged-line skipping.
- `java PassStoreTest` – add/find/missing ID/empty and malformed line/route listing.
- `java LineCounter` – creates 1 MiB, 10 MiB and 50 MiB temporary files and times byte-at-a-time vs buffered counting.

The five-column simple CSV implementation rejects comma/newline inside fields. Use a CSV library for general quoted CSV. Record actual timing results in the report.
