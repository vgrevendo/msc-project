package testbench.tests;

import java.util.List;

import testbench.Test;
import testbench.TestException;
import testbench.lister.TestLister;
import algorithms.membership.MBSDecisionAlgorithm;
import algorithms.tools.ResultsContainer;
import automata.Automaton;

/**
 * A multiple membership test, comparing several algorithms (LDFTS, BFLGS, ...).
 * Take a lot of words of various lengths and compare performances. 
 * @author vincent
 */
public class ListMembershipTest extends Test {
	private final TestLister<List<Integer>> twg;
	private final MBSDecisionAlgorithm[] algorithms;
	
	//Internal stats
	private int successMemberships = 0;
	private int[] totalTimes;
	
	public ListMembershipTest(Automaton a, 
								MBSDecisionAlgorithm[] algorithms, 
								TestLister<List<Integer>> twg) {
		super("Listed Membership Checks", a);
		this.algorithms = algorithms;
		this.twg = twg;
		
		maxProgression = twg.size()*algorithms.length;
		totalTimes = new int[algorithms.length];
	}
	
	public ListMembershipTest(Automaton a, 
			MBSDecisionAlgorithm[] algorithms, 
			TestLister<List<Integer>> twg,
			String outputFilename) {
		super("Listed Membership Checks", a, outputFilename);
		this.algorithms = algorithms;
		this.twg = twg;
		
		maxProgression = twg.size()*algorithms.length;
		totalTimes = new int[algorithms.length];
	}

	@Override
	protected void run() throws TestException {
		//Consistency checks are integrated in the tests themselves
		boolean result = false;
		//make the tests
		//For each word, test each algorithm
		for(List<Integer> testWord : twg) {
			System.out.println("Current word size: " + testWord.size() + " symbols");
			
			boolean previousResult = false;
			
			for(int algIndex = 0; algIndex < algorithms.length; algIndex++) {
				signalProgression();
				
				MBSDecisionAlgorithm algorithm = algorithms[algIndex];
				
				//TEST CORE
				long cTime = System.currentTimeMillis();			
				result = algorithm.decide(testWord);
				long testTime = System.currentTimeMillis()-cTime;
				
				//Record results
				successMemberships += result ? 1 : 0;
				rc.addSessionNumber(algorithm.name, "Time", (int)testTime);
				totalTimes[algIndex] += testTime;
				
				if(algIndex > 0 && previousResult != result)
					throw new TestException("Consistency failure: algorithms disagree on " + testWord.toString());
				else
					previousResult = result;
				
				rc.addSessionNumber(algorithm.name, "Word Size", testWord.size());
				algorithm.yieldStatistics(rc);
			}
		}
		
		signalProgression();
		
		
		//print all useful data
		for(int algIndex = 0; algIndex < algorithms.length; algIndex++) {
			MBSDecisionAlgorithm algorithm = algorithms[algIndex];
			
			addStats(rc.getSession(algorithm.name));
		}
	}
	
	@Override
	public void customPrint(ResultsContainer rc) {
		for(int algIndex = 0; algIndex < algorithms.length; algIndex++) {
			MBSDecisionAlgorithm algorithm = algorithms[algIndex];
			
			rc.println(algorithm + " total execution time:   " + prettyPrintMillis(totalTimes[algIndex]));
			if(twg.size() > 0)
				rc.println(algorithm + " average execution time: " + prettyPrintMillis(totalTimes[algIndex]/twg.size()));
			else
				rc.println(algorithm + " average execution time unkown.");
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
