---------------------------------------------
-- Benchmarks resource file
-- Abbreviations:
-- SIP : Safe Iter Property
-- HNP : Has Next Property
-- UMI : Unsafe Map Iter (Property)
-- TSS : Tainted Source Sink (Property)
---------------------------------------------
-- TOMCAT -----------------------------------
--dacapo	:tomcat		:has_next
--dacapo	:tomcat		:unique_servlet_output
dacapo	:tomcat		:safe_iter
dacapo	:tomcat		:unsafe_map_iter
-- AVRORA -----------------------------------
--dacapo	:avrora		:has_next
--dacapo	:avrora		:safe_iter
--dacapo	:avrora		:unsafe_map_iter
-- PMD --------------------------------------
--dacapo	:pmd		:has_next
--dacapo	:pmd		:safe_iter
--dacapo	:pmd		:unsafe_map_iter
-- XALAN ------------------------------------
--dacapo	:xalan		:has_next
--dacapo	:xalan		:safe_iter
--dacapo	:xalan		:unsafe_map_iter
-- CUSTOM -----------------------------------
other	:hnp_custom	:has_next			:testbench/programs/HasNextPropertyTracer MULTIPLE 2000 2000:testbench.programs.HasNextPropertyTracer main main
other	:sip_custom	:safe_iter			:testbench/programs/SafeIterProgram:testbench.programs.SafeIterProgram main main
other	:tss_custom :tainted_sink_source:testbench/programs/SourceSinkProgram 100 1000:testbench.programs.SourceSinkProgram main main
