"""
FIRST OPTION: "collect"
- The translations folder contains translation rules.
Format: "property_name.trf"
- The resources file "benchmarks.res" contains the names of all interesting benchmarks,
and attached properties to be checked.
Format: just a list of lines with one name per line.
If it is a Dacapo benchmark, write "dacapo:benchmark_name:property_name" (java command is automatic)
Else write: "other:benchmark_name:property_name:java_command_suffix"
where java_command suffix is the main class to be executed with its parameters.
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
HOME_TESTS_RES_PATH = os.path.join(ROOT, "home-tests.res")
REF_TESTS_RES_PATH = os.path.join(ROOT, "ref-tests.res")
DACAPO_PATH = "dacapo-9.12-bach.jar"
DEBUGGER_PORT = "42892"
DEBUG_LIB_CMD = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address="+str(DEBUGGER_PORT)
JDI_JAR_PATH = "bin:/usr/lib/jvm/java-7-openjdk-amd64/lib/tools.jar:"+DACAPO_PATH
TRF_TRACER = "testbench/programs/tracer/TRFTracer"
DACAPO_MAIN_CLASS = "MyCallback"
DACAPO_ENTRY_METHOD = "start"
DACAPO_EXIT_METHOD = "stop"
TRACES_FOLDER = os.path.join(ROOT, "traces")
AUTOMATA_FOLDER = os.path.join(ROOT, "automata")
RUN_RESULTS_FOLDER = os.path.join(ROOT, "results")
RVMONITOR_JAR_PATH = "/data/data/Programmation/Eclipse/workspaces/ubuntu/javamop/lib/rt.jar"

#NEW TRACER
NT_AGENT = "-javaagent:agent.jar="
NT_CP = "-Xbootclasspath/a:bin:agent.jar"
NT_USE_NT = True

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
    bmarks = [bmark.strip(' \t\n\r') for bmark in bmarks if not bmark.startswith("--")]
    
    for bmark in bmarks:
        print "    * Found " + bmark
        
    print "(i) Will now run " + str(len(bmarks)) + " trace collections"
    
    for bmark in bmarks:
        run_trace(bmark, files)
    
def parse_benchmarks():
    with open(BENCHMARKS_RES_PATH) as f:
        return f.readlines()
            
def run_trace(bmark, trf_files):
    tokens = bmark.split(":")
    tokens = [token.strip(' \t\n\r') for token in tokens]
    
    # Read parameters
    #  TRF
    trf_name = tokens[2]
    trf_filename = trf_name + ".trf"
    trf = os.path.join(TRF_FOLDER_PATH, trf_filename)
    
    #  BENCHMARK
    dacapo = "dacapo" in bmark
    bm_name = tokens[1]
    
    print "   - Running " + trf_name + " on " + bm_name
    
    output_filename = trf_name + "-" + bm_name + ".tr"
    output_path = os.path.join(TRACES_FOLDER,output_filename)
    
    if dacapo: 
        if NT_USE_NT:
            #not very efficient...
            agent_options = "trf="
            agent_options += trf
            agent_options += ",out="
            agent_options += output_path
            agent_options += ",hook="
            agent_options += DACAPO_MAIN_CLASS
            agent_options += ",inmethod="
            agent_options += DACAPO_ENTRY_METHOD
            agent_options += ",outmethod="
            agent_options += DACAPO_EXIT_METHOD
            agent_options += ",limit=false"
            target_cmd = ["java", NT_AGENT + agent_options, NT_CP, "-jar",
                          DACAPO_PATH, "-c", DACAPO_MAIN_CLASS, "-s", "large",
                          bm_name]
            debug_cmd = None
        else:
            target_cmd = ["java", DEBUG_LIB_CMD, "-jar", DACAPO_PATH, "-c", "MyCallback",
                          "-s", "large", bm_name]
            debug_cmd = ["java", "-cp", JDI_JAR_PATH, TRF_TRACER, DEBUGGER_PORT, DACAPO_MAIN_CLASS,
                         DACAPO_ENTRY_METHOD, DACAPO_EXIT_METHOD, trf, output_path]
    else: #other
        if NT_USE_NT:
            hook_options = tokens[4].split(" ")
            
            #Build agent parameters
            agent_options = "trf="
            agent_options += trf
            agent_options += ",out="
            agent_options += output_path
            agent_options += ",hook="
            agent_options += hook_options[0]
            agent_options += ",inmethod="
            agent_options += hook_options[1]
            agent_options += ",outmethod="
            agent_options += hook_options[2]
            agent_options += ",limit="
            agent_options += hook_options[3]            
            
            #Build command
            target_cmd = ["java", NT_AGENT + agent_options, NT_CP]
            target_cmd += tokens[3].split(" ")
            debug_cmd = None
        else:
            target_cmd = ["java", DEBUG_LIB_CMD, "-cp", "bin"]
            target_cmd += tokens[3].split(" ")
            debug_cmd = ["java", "-cp", JDI_JAR_PATH, TRF_TRACER, DEBUGGER_PORT]
            debug_cmd += tokens[4].split(" ")
            debug_cmd += [trf, output_path]
        
    # Run commands intelligently
    print "(x) TARGET: " + " ".join(target_cmd)
    
    if debug_cmd is not None:
        print "(x) DEBUGGER: " + " ".join(debug_cmd)
        target = subprocess.Popen(target_cmd)
        time.sleep(2)
    
        subprocess.call(debug_cmd)
        target.terminate()
        time.sleep(2)
        
    else:
        subprocess.call(target_cmd)
        time.sleep(2)

def test_references():
    print "(i) Testing REF algorithms"
    print "(i) Parsing file 'ref-tests.res'..."
    
    torun = parse_ref_tests_res()
    
    print "    Found " + str(len(torun)) + " executions"
    print "(i) Initiating run sequence:"
    
    for e in torun:
        run_ref_test(e)

def test_home():
    print "(i) Testing HOME algorithms"
    print "(i) Parsing file 'home-tests.res'..."
    
    torun = parse_home_tests_res()
    
    print "    Found " + str(len(torun)) + " executions"
    print "(i) Initiating run sequence:"
    
    for e in torun:
        run_home_test(e)

def parse_home_tests_res():
    with open(HOME_TESTS_RES_PATH) as f:
        lines = f.readlines()
        
        runs = []
        for line in lines:
            if line.startswith("--"):
                continue
            
            tokens = line.split(":")
            run = {}
            run["property"] = tokens[0].strip(' \t\r')
            run["benchmark"] = tokens[1].strip(' \t\r')
            run["algorithm"] = tokens[2].strip(' \t\r')
            run["difficulty"] = tokens[3].strip(' \t\r')
            run["jvmparams"] = tokens[4].strip(' \t\r\n')
            
            runs.append(run)
        
        return runs

def run_home_test(rundata):
    #Consitute run command
    trace_name = rundata["property"] + "-" + rundata["benchmark"] + ".tr"
    trace_path = os.path.join(TRACES_FOLDER, trace_name)
    automaton_path = os.path.join(AUTOMATA_FOLDER, rundata["property"] + ".fma")
    output_path = os.path.join(RUN_RESULTS_FOLDER, 
                               rundata["property"] + "-" + rundata["benchmark"] +
                               "-" + rundata["algorithm"] + ".csv")
    
    cmd = ["java", rundata["jvmparams"], "-cp", "bin", 
           "testbench/Testbench", "mbs",
           rundata["algorithm"], automaton_path,
           trace_path, rundata["difficulty"], output_path]
    
    print "(x) RUN: " + " ".join(cmd)
    
    subprocess.call(cmd)
    time.sleep(2)
    
def parse_ref_tests_res():
    with open(REF_TESTS_RES_PATH) as f:
        lines = f.readlines()
        
        runs = []
        for line in lines:
            if line.startswith("--"):
                continue
            
            tokens = line.split(":")
            run = {}
            run["property"] = tokens[0].strip(' \t\r')
            run["benchmark"] = tokens[1].strip(' \t\r')
            run["algorithm"] = tokens[2].strip(' \t\r')
            run["difficulty"] = tokens[3].strip(' \t\r')
            run["jvmparams"] = tokens[4].strip(' \t\r\n')
            
            runs.append(run)
        
        return runs
    
def run_ref_test(rundata):
    #Consitute run command
    trace_name = rundata["property"] + "-" + rundata["benchmark"] + ".tr"
    trace_path = os.path.join(TRACES_FOLDER, trace_name)
    automaton_path = os.path.join(AUTOMATA_FOLDER, rundata["property"] + ".fma")
    output_path = os.path.join(RUN_RESULTS_FOLDER, 
                               rundata["property"] + "-" + rundata["benchmark"] +
                               "-" + rundata["algorithm"] + ".csv")
    
    classpath = "bin:" + RVMONITOR_JAR_PATH
    
    cmd = ["java", rundata["jvmparams"], "-cp", classpath, 
           "testbench/Testbench", "mbs",
           rundata["algorithm"], automaton_path,
           trace_path, rundata["difficulty"], output_path]
    
    print "(x) RUN: " + " ".join(cmd)
    
    subprocess.call(cmd)
    time.sleep(2)

if __name__ == "__main__":
    main(sys.argv[1:])