package testbench;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import algorithms.tools.ResultsContainer;
import automata.RegisterAutomaton;

public abstract class Test {
	public static final int MIN_SIGNAL_PERIOD = 5; //in seconds
	public static final String CSV_PATH_ROOT = "gen/test";
	
	protected final ResultsContainer rc;
	private final String name;
	protected final RegisterAutomaton a;
	
	//Characteristics
	private long runtime = 0L;
	private int progression = -1;
	protected int maxProgression = 0;
	
	//Results storage
	private final List<String> csvLabels = new ArrayList<>();
	private final List<int[]> csvLists = new ArrayList<>();
	private final List<String[]> csvStringLists = new ArrayList<>();
	
	private long lastSigTime = 0L; 
	
	public Test(String name, RegisterAutomaton a) {
		this.name = name;
		this.a = a;
		this.rc = ResultsContainer.getContainer();
	}
	
	/**
	 * Run the underlying test and record the numbers.
	 */
	public void test() {
		System.out.println("-- This is test [[" + name + "]] --");
		System.out.println("> Preparing resources...");
		prepare();
		System.out.println("> Resources are ready for use.");
		
		System.out.println("> Running test core: acquisition...");
		long currentTime = System.currentTimeMillis();
		
		try {
			run();
		} catch(TestException e) {
			rc.println("An error occurred during the test:");
			rc.print(e.getMessage());
			e.printStackTrace();
		}
		
		runtime = System.currentTimeMillis() - currentTime;
		signalProgression();
		
		System.out.println("> End of acquisition.");
		
		//Print to results container
		System.out.println("> Outputting results to results containers...");
		outputResults();
		System.out.println("> End of test.");
		System.out.println("-------------------------");
	}
	
	//Implement/override these for more functionalities
	protected abstract void run() throws TestException;
	protected void customPrint(ResultsContainer rc) {
	}
	protected abstract void prepare() ;
	
	//Tools
	private void outputResults() {
		//Flush results that tests might have output
		rc.clear();
		
		//Write out own results
		rc.println("[[PERFORMANCE TEST RESULTS: " + name + "]]");
		rc.println("Total runtime: " + prettyPrintMillis(runtime));
		
		customPrint(rc);
		
		try {
			String csvFileName = outputCsv();
			if(csvFileName != null) {
				rc.println("Test printed CSV datafile at " + csvFileName);
			}
		} catch (Exception e) {
			rc.println("CSV output encountered an error: " + e.getMessage());
			rc.println("CSV output aborted.");
		}
		
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
		progression++;
		
		if(lastSigTime + MIN_SIGNAL_PERIOD*1000 < System.currentTimeMillis() || progression*10 % maxProgression == 0) {
			System.out.print("Test '" + name + "' progression: " + (((double)progression)/((double)maxProgression))*100.0 + "% ");
			System.out.println("|" + progression + " out of " + maxProgression + " problem instances");
			lastSigTime = System.currentTimeMillis();
		}
	}

	protected void addCsvColumn(int[] elements, String label) {
		csvLabels.add(label);
		csvLists.add(elements);
	}
	
	protected void addCsvColumn(String[] elements, String label) {
		csvLabels.add(label);
		csvStringLists.add(elements);
	}
	
	private String outputCsv() throws FileNotFoundException {
		if(csvLists.isEmpty())
			return null;
		
		String filename = chooseFileName();
		PrintWriter pw = new PrintWriter(filename);
		
		final int length = csvLists.get(0).length;
		
		for(String label : csvLabels) {
			pw.print(label + ";");
		}
		pw.print("\n");
		
		for(int i = 0; i < length; i++) {
			for(int[] elements : csvLists) {
				pw.print(elements[i] + ";");
			}
			for(String[] elements : csvStringLists) {
				pw.print(elements[i] + ";");
			}
			pw.print("\n");
		}
		
		pw.close();
		return filename;
	}
	
	private String chooseFileName() {
		int id = 0;
		
		while((new File(CSV_PATH_ROOT + id + ".csv")).exists()) {
			id ++;
		}
		
		return CSV_PATH_ROOT + id + ".csv";
	}
}
