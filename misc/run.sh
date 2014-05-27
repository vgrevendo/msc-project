#! /bin/bash

# PATHS given here might be platform-dependent, look
#  out for any errors related to "classes not found"

# -- 0. Compile all resources --
#  The target program (simulating the hasNext behaviour)
javac -d bin -sourcepath src src/testbench/programs/HasNextPropertyTracer.java
#  The debugger, attached to trace
javac -d bin -sourcepath src -cp /usr/lib/jvm/java-7-openjdk-amd64/lib/tools.jar  src/testbench/programs/tracer/Tracer.java
#  The "runtime" verification Testbench
javac -d bin -sourcepath src src/testbench/Testbench.java

# -- 1. Run target program --
#  We're expecting about 400,000 entries here, but
#  parameters are chosen randomly so it might be necessary to
#  run the whole sequence again if the number of entries is
#  insufficient.
#  The two parameters at the end are "maximum number of elts in the list",
#  and "maximum numbers of iterators" over those elements.
java -cp bin -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=42890 testbench/programs/HasNextPropertyTracer MULTIPLE 50 50 &

# -- 2. Attach debugger --
#  Target program is now waiting, and we want to attach
#  the tracer at the specified address.
java -cp bin:/usr/lib/jvm/java-7-openjdk-amd64/lib/tools.jar testbench/programs/tracer/Tracer 42890 testbench.programs.HasNextPropertyTracer

# -- 3. Perform verification --
#  With these parameters, the verification is bound to fail.
#  For 60,000 symbols analysed, my machine had a "GC overhead limit exceed" OutOfMemoryError thrown.
java -cp bin testbench/Testbench hasNextProperty-STRICT LATEST 0.5