package testbench.programs.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

public class Agent {
	private static boolean signalledRetransformationFail = false;
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
		ClassFileTransformer cft = new Inspector(Tracer.getTracer());
		instr.addTransformer(cft, true); //"I'm OK with retransforming classes!"
	
		for(Class<?> c: instr.getAllLoadedClasses()) {
			if(c.getName().endsWith("HashIterator"))
				continue;
				
			try {
				instr.retransformClasses(c);
			} catch (Throwable e) {
				if(!signalledRetransformationFail) {
					System.out.println("(w) Could not retransform some classes because not modifiable, skipping these.");
					signalledRetransformationFail = true;
				}
			}
		}
		
		System.out.println("(i) Handing over to instrumented target program");
	}
}
