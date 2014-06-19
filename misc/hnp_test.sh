#! /bin/bash

# PATHS given here might be platform-dependent, look
#  out for any errors related to "classes not found"

# -- 0. Compile all resources --
#  The target program (simulating the hasNext behaviour)
javac -d bin -sourcepath src src/testbench/programs/HasNextPropertyTracer.java
#  The debugger, attached to trace
javac -d bin -sourcepath src -cp /usr/lib/jvm/java-7-openjdk-amd64/lib/tools.jar  src/testbench/programs/tracer/Tracer.java
#  The "runtime" verification Testbench
# javac -d bin -sourcepath src src/testbench/Testbench.java

# -- 1. Run target program --
#  We're expecting a lot of entries here, but
#  parameters are chosen randomly so it might be necessary to
#  run the whole sequence again if the number of entries is
#  insufficient.
#  The two parameters at the end are "maximum number of elts in the list",
#  and "maximum numbers of iterators" over those elements.
java -cp bin -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=42890 testbench/programs/HasNextPropertyTracer MULTIPLE 500 500 &

# -- 2. Attach debugger --
#  Target program is now waiting, and we want to attach
#  the tracer at the specified address.
java -cp bin:/usr/lib/jvm/java-7-openjdk-amd64/lib/tools.jar testbench/programs/tracer/Tracer 42890 testbench.programs.HasNextPropertyTracer main main res/has_next.trf

# -- 3. Perform verification --
# java -cp bin testbench/Testbench auto gen/trace3.tr res/has_next.trf SYNTH:res/has_next.mra default
