# Q5 – Cinema Seat Booking Race Condition

Compile: `javac SeatBooking.java`  
Run synchronized version: `java SeatBooking`  
Run race demonstration: `java SeatBooking race`

The default mode synchronizes the check-and-book operation. The `race` mode deliberately removes synchronization and inserts a short pause to make the race easier to observe. Race reproduction is timing-dependent.
