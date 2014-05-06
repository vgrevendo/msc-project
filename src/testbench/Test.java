package testbench;

import algorithms.tools.ResultsContainer;
import automata.RegisterAutomaton;

public abstract class Test {
	protected final ResultsContainer rc;
	private final String name;
	protected final RegisterAutomaton a;
	
	//Characteristics
	private long runtime = 0L;
	protected int progression = 0;
	protected int maxProgression = 0;
	
	public Test(String name, RegisterAutomaton a) {
		this.name = name;
		this.a = a;
		this.rc = ResultsContainer.getContainer();
	}
	
	/**
	 * Run the underlying test and record the numbers.
	 */
	public void test() {
		long currentTime = System.currentTimeMillis();
		
		try {
			run();
		} catch(Exception e) {
			rc.println("An error occurred during the test:");
			rc.print(e.getMessage());
			e.printStackTrace();
		}
		
		runtime = System.currentTimeMillis() - currentTime;
		signalProgression();
		
		//Print to results container
		outputResults();
	}
	
	//Implement/override these for more functionalities
	protected abstract void run();
	protected void customPrint(ResultsContainer rc) {
	}
	
	//Tools
	private void outputResults() {
		//Flush results that tests might have output
		rc.clear();
		
		//Write out own results
		rc.println("[[PERFORMANCE TEST RESULTS: " + name + "]]");
		rc.println("Total runtime: " + prettyPrintMillis(runtime));
		
		customPrint(rc);
		
		rc.println("------------------------------------------");
		
		rc.commit();
	}

	protected int pickFrom(int max) {
		return (int)(Math.random()*(double)max);
	}
	
	protected String prettyPrintMillis(long time) {
		return time + "ms (" + ((double)time)/1000.0 + "s)";
	}
	
	protected void signalProgression() {
		System.out.println("Test '" + name + "' progression: " + (((double)progression)/((double)maxProgression))*100.0 + "%");
	}
}
