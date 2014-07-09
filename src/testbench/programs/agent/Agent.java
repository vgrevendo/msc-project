package testbench.programs.agent;

import java.lang.instrument.Instrumentation;

public class Agent {
	/**
	 * The agent's entry point.
	 */
	public static void premain(String agentArgument, Instrumentation instr) {
		System.out.println("[[AGENTLIB ENTRY POINT]]");
		
		System.out.println("(i) Loading tracer...");
		try {
			Tracer.loadRules(agentArgument);
		} catch (Exception e) {
			System.out.println("(e) Tracer loading failed, here's why:");
			e.printStackTrace();
			System.out.println("Aborting...");
			System.exit(0);
		}
		
		System.out.println("(i) Loading instrumentation...");
		
		System.out.println("(i) Handing over to instrumented target program");
	}
}
