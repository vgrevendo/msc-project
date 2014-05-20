package testbench.tests;

import java.util.Arrays;
import java.util.List;

import testbench.Test;
import testbench.TestException;
import testbench.lister.TestLister;
import algorithms.membership.MBSDecisionAlgorithm;
import algorithms.tools.ResultsContainer;
import automata.RegisterAutomaton;

/**
 * A multiple membership test, comparing several algorithms (LDFTS, BFLGS, ...).
 * Take a lot of words of various lengths and compare performances. 
 * @author vincent
 */
public class ListMembershipTest extends Test {
	private final TestLister<int[]> twg;
	private final MBSDecisionAlgorithm[] algorithms;
	
	//Results
	private final boolean[] results;
	private final int[][] nodesExpanded;
	private final int[][] maxFrontierSize;
	private final int[][] times;
	private final long[] totalTimes;
	
	public ListMembershipTest(RegisterAutomaton a, 
								MBSDecisionAlgorithm[] algorithms, 
								TestLister<int[]> twg) {
		super("Listed Membership Checks", a);
		this.algorithms = algorithms;
		this.twg = twg;
		
		maxProgression = twg.size()*algorithms.length;
		
		totalTimes = new long[algorithms.length];
		results = new boolean[twg.size()];
		nodesExpanded = new int[algorithms.length][twg.size()];
		maxFrontierSize = new int[algorithms.length][twg.size()];
		times = new int[algorithms.length][twg.size()];
	}

	@Override
	protected void run() throws TestException {
		//Consistency checks are integrated in the tests themselves
		boolean result = false;
		String[] testWordStrings = new String[twg.size()];
		
		//make the tests
		//For each word, test each algorithm
		for(int[] testWord : twg) {
			for(int algIndex = 0; algIndex < algorithms.length; algIndex++) {
				signalProgression();
				
				MBSDecisionAlgorithm algorithm = algorithms[algIndex];
				
				//TEST CORE
				long cTime = System.currentTimeMillis();			
				result = algorithm.decide(a, testWord);
				long testTime = System.currentTimeMillis()-cTime;
				
				//Record results
				totalTimes[algIndex] += testTime;
				times[algIndex][twg.getIndex()] = (int) testTime;
				
				if(algIndex > 0 && results[twg.getIndex()] != result)
					throw new TestException("Consistency failure: algorithms disagree on " + Arrays.toString(testWord));
				else
					results[twg.getIndex()] = result;
				
				List<Integer> numbers = rc.getNumbersList();
				if(numbers.size() > 0) {
					nodesExpanded[algIndex][twg.getIndex()] = numbers.get(0);
					maxFrontierSize[algIndex][twg.getIndex()] = numbers.get(1);
				}
			}
			
			testWordStrings[twg.getIndex()] = Arrays.toString(testWord);
		}
		
		signalProgression();
		
		
		//print all useful data
		for(int algIndex = 0; algIndex < algorithms.length; algIndex++) {
			MBSDecisionAlgorithm algorithm = algorithms[algIndex];
			
			addCsvColumn(nodesExpanded[algIndex], algorithm + " nodes");
			addCsvColumn(maxFrontierSize[algIndex], algorithm + " frontier");
			addCsvColumn(times[algIndex], algorithm + " time");
		}
		addCsvColumn(testWordStrings, "Test word");
	}
	
	@Override
	public void customPrint(ResultsContainer rc) {
		int successMemberships = 0;
		for(boolean b: results) {
			successMemberships += b ? 1 : 0;
		}
		
		for(int algIndex = 0; algIndex < algorithms.length; algIndex++) {
			MBSDecisionAlgorithm algorithm = algorithms[algIndex];
			
			rc.println(algorithm + " total execution time:   " + prettyPrintMillis(totalTimes[algIndex]));
			rc.println(algorithm + " average execution time: " + prettyPrintMillis(totalTimes[algIndex]/twg.size()));
		}
		
		rc.println("");
		rc.println("Total number of success memberships: " + successMemberships + "/" + twg.size());
	}

	@Override
	protected void prepare() {
		//This method will allow to pre-process automata if necessary.
		for(MBSDecisionAlgorithm da : algorithms) {
			System.out.print("Preparing " + da + "... ");
			da.setAutomaton(a);
			System.out.println("[OK]");
		}
	}

}
