package testbench.programs.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.LinkedList;
import java.util.Queue;

public class Agent {
	private static boolean signalledRetransformationFail = false;
	public final static boolean DEBUG_MODE = false;
	
	private static final Queue<Class<?>> toRetransform = new LinkedList<Class<?>>();
	/**
	 * The agent's entry point.
	 */
	public static void premain(String agentArgument, Instrumentation instr) {
		System.out.println("------ [[AGENTLIB ENTRY POINT]] -------");
		
		System.out.println("(i) Loading tracer...");
		try {
			Tracer.loadRules(agentArgument);
		} catch (Exception e) {
			System.out.println();
			System.out.println("(e) Tracer loading failed, here's why:");
			e.printStackTrace();
			System.out.println("Aborting...");
			System.exit(0);
		}
		
		System.out.println("    DONE");
		
		System.out.println("(i) Loading instrumentation...");
		ClassFileTransformer cft = new Inspector(Tracer.getTracer());
		instr.addTransformer(cft, true); //"I'm OK with retransforming classes!"
		
		System.out.println("    DONE");
	
		//Load classes to be instrumented
		for(Class<?> c: instr.getAllLoadedClasses()) {
			toRetransform.add(c);
		}
		
		while(!toRetransform.isEmpty()) {
			Class<?> c = toRetransform.poll();
			
			if(c == null)
				continue;
			
			try {
				instr.retransformClasses(c);
			} catch (Throwable e) {
				if(c.getName().contains("Int")) {
					System.out.println("Exception occurred: ");
					e.printStackTrace();
				}
				if(!signalledRetransformationFail) {
					System.out.println("(w) Could not retransform some classes because not modifiable, skipping these.");
					signalledRetransformationFail = true;
				}
			}
		}
		
		System.out.println("(i) Handing over to instrumented target program");
	}
	
	public static void addToRetransform(Class<?> c) {
		toRetransform.add(c);
	}
}
