"""
FIRST OPTION: "collect"
- The translations folder contains translation rules.
Format: "property_name.trf"
- The resources file "benchmarks.res" contains the names of all interesting benchmarks.
Format: just a list of lines with one name per line.
If it is a Dacapo benchmark, write "dacapo:benchmark_name"
Else write: "other:java_command"
- Run all benchmarks by applying the TRF debugger and store the traces in "trace".
Output format: "property_name-benchmark_name.tr"
These traces are of course pre translated

SECOND OPTION: "test-ref"
- The traces folder contains benchmark traces as explained above.
Format: "property_name-benchmark_name.tr"
- The resources file "references.res" contains the names of all reference algorithms.
Format: one Testbench run option per line: "property_name: algorithm_options"
- Run each reference algorithm with each appropriate trace file.
Results are recorded as test files with names: "property_name-benchmark_name-algorithm_name.csv"
where algorithm_name is the first testbench option (unique).

THIRD OPTION: "test-home"
- The traces folder contains benchmark traces as explained above.
Format: "property_name-benchmark_name.tr"
- The resources file "home.res" contains the names of all homemade algorithms.
Format: one Testbench run option per line: "property_name: algorithm_options"
- Run each homemade algorithm with each appropriate trace file.
Results are recorded as test files with names: "property_name-benchmark_name-algorithm_name.csv"
"""

import sys
import os
import subprocess
import time

ROOT = os.path.dirname(__file__)
TRF_FOLDER_PATH = os.path.join(ROOT, "trf/")
BENCHMARKS_RES_PATH = os.path.join(ROOT, "benchmarks.res")
DACAPO_PATH = "dacapo-9.12-bach.jar"
DEBUGGER_PORT = "42891"
DEBUG_LIB_CMD = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address="+str(DEBUGGER_PORT)
JDI_JAR_PATH = "bin:/usr/lib/jvm/java-7-openjdk-amd64/lib/tools.jar:"+DACAPO_PATH
TRF_TRACER = "testbench/programs/tracer/TRFTracer"
DACAPO_MAIN_CLASS = "MyCallback"
DACAPO_ENTRY_METHOD = "start"
DACAPO_EXIT_METHOD = "stop"


def main(argv):
    os.chdir(os.path.abspath(os.path.join(ROOT, os.pardir)))
    
    cmd_dict = {"collect": collect_traces,
                "test-ref": test_references,
                "test-home": test_home}
    
    if len(argv) == 0:
        print "Must specify an operation option!"
        return
    
    arg = argv[0]
    if cmd_dict.has_key(arg):
        cmd_dict[arg]()
    else:
        print "Usage: collect|test-ref|test-home"
            
    print "DONE"
    
def collect_traces():
    print "(i) About to collect traces"
    print "(i) Analysing parameters..."
    print "   - translation rules in the 'trf' folder"
    
    files = os.listdir(TRF_FOLDER_PATH)
    files = [os.path.join(TRF_FOLDER_PATH, file) for file in files if file[-4:] == ".trf"]
    
    if len(files) == 0:
        print "(E) No translation files found! ABORTING"
        return
    
    for trf in files:
        print "    * Found " + trf
        
    print "   - looking for benchmarks in 'benchmarks.res'"
    
    bmarks = parse_benchmarks()
    bmarks = [bmark.strip(' \t\n\r') for bmark in bmarks]
    
    for bmark in bmarks:
        print "    * Found " + bmark
        
    print "(i) Will now run " + str(len(bmarks)*len(files)) + " trace collections"
    
    for trf in files:
        for bmark in bmarks:
            run_trace(trf, bmark)
    
def parse_benchmarks():
    with open(BENCHMARKS_RES_PATH) as f:
        return f.readlines()
            
def run_trace(trf, bmark):
    trf_name = os.path.basename(trf)
    bm_name = bmark[7:]
    print "   - Running " + trf_name + " on " + bmark
    
    output_filename = trf_name[:-4] + "-" + bm_name + ".trf"
    output_path = os.path.join(ROOT,"traces",output_filename)
    
    if "dacapo" in bmark:
        target_cmd = ["java", DEBUG_LIB_CMD, "-jar", DACAPO_PATH, "-c", "MyCallback",
                      "-s", "large", bm_name]
        print "(x) TARGET: " + " ".join(target_cmd)
        debug_cmd = ["java", "-cp", JDI_JAR_PATH, TRF_TRACER, DEBUGGER_PORT, DACAPO_MAIN_CLASS,
                     DACAPO_ENTRY_METHOD, DACAPO_EXIT_METHOD, trf, output_path]
        print "(x) DEBUGGER: " + " ".join(debug_cmd)
        
        target = subprocess.Popen(target_cmd)
        time.sleep(2)
        subprocess.call(debug_cmd)
        target.terminate()
        time.sleep(2)
    else:
        print "Not implemented!"

def test_references():
    pass

def test_home():
    pass

if __name__ == "__main__":
    main(sys.argv[1:])