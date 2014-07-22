---------------------------------------------
-- Benchmarks resource file
-- Abbreviations:
-- SIP : Safe Iter Property
-- HNP : Has Next Property
-- UMI : Unsafe Map Iter (Property)
-- TSS : Tainted Source Sink (Property)
---------------------------------------------
-- HAS_NEXT ---------------------------------
--dacapo	:tomcat		:has_next
--dacapo	:avrora		:has_next
--dacapo	:pmd		:has_next
--dacapo	:xalan		:has_next
--dacapo	:batik		:has_next
--dacapo	:eclipse	:has_next
--dacapo	:fop		:has_next
--dacapo	:h2			:has_next
--dacapo	:jython		:has_next
--dacapo	:luindex	:has_next
--dacapo	:lusearch	:has_next
--dacapo	:sunflow	:has_next
--dacapo	:tradebeans	:has_next
--dacapo	:tradesoap	:has_next
-- SAFE_ITER --------------------------------
--dacapo	:tomcat		:safe_iter
--dacapo	:avrora		:safe_iter
--dacapo	:pmd		:safe_iter
--dacapo	:xalan		:safe_iter
--dacapo	:batik		:safe_iter
--dacapo	:eclipse	:safe_iter
--dacapo	:fop		:safe_iter
--dacapo	:h2			:safe_iter
--dacapo	:jython		:safe_iter
--dacapo	:luindex	:safe_iter
--dacapo	:lusearch	:safe_iter
--dacapo	:sunflow	:safe_iter
--dacapo	:tradebeans	:safe_iter
--dacapo	:tradesoap	:safe_iter
-- UNSAFE_MAP_ITER --------------------------
dacapo	:tomcat		:unsafe_map_iter
dacapo	:avrora		:unsafe_map_iter
dacapo	:pmd		:unsafe_map_iter
dacapo	:xalan		:unsafe_map_iter
dacapo	:batik		:unsafe_map_iter
dacapo	:eclipse	:unsafe_map_iter
dacapo	:fop		:unsafe_map_iter
dacapo	:h2			:unsafe_map_iter
dacapo	:jython		:unsafe_map_iter
dacapo	:luindex	:unsafe_map_iter
dacapo	:lusearch	:unsafe_map_iter
dacapo	:sunflow	:unsafe_map_iter
dacapo	:tradebeans	:unsafe_map_iter
dacapo	:tradesoap	:unsafe_map_iter
-- MISC -------------------------------------
--dacapo	:tomcat		:unique_servlet_output
-- CUSTOM -----------------------------------
other	:hnp_custom	:has_next			:testbench/programs/HasNextPropertyTracer MULTIPLE 2000 2000:testbench.programs.HasNextPropertyTracer main main true
other	:sip_custom	:safe_iter			:testbench/programs/SafeIterProgram:testbench.programs.SafeIterProgram main main
other	:tss_custom	:tainted_sink_source		:testbench/programs/SourceSinkProgram 1000 100000:testbench.programs.SourceSinkProgram main main true
