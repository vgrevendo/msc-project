package testbench.tests;

import java.util.Iterator;
import java.util.List;

import algorithms.membership.MBSDecisionAlgorithm;
import algorithms.tools.ResultsContainer;
import automata.RegisterAutomaton;
import testbench.Test;
import testbench.TestException;
import testbench.lister.TestLister;

/**
 * This is mainly the same code as in listed membership,
 * but with several different automata
 * @author vincent
 *
 */
public class AsymptoticMembershipTest extends Test {
	private final TestLister<List<Integer>> twg;
	private final TestLister<RegisterAutomaton> ag;
	private final MBSDecisionAlgorithm[] algorithms;
	
	//Results
	private final int[][] totalTimes;
	private int successMemberships = 0;

	public AsymptoticMembershipTest(TestLister<RegisterAutomaton> ag, 
									TestLister<List<Integer>> twg,
									MBSDecisionAlgorithm[] algorithms) {
		//We don't give an automaton to the super class, because we don't need any
		super("Asymptotic MBS", null);
		this.algorithms = algorithms;
		this.ag = ag;
		this.twg = twg;
		
		maxProgression = twg.size()*algorithms.length;
		totalTimes = new int[algorithms.length][twg.size()];
	}

	@Override
	protected void run() throws TestException {
		//Consistency checks are integrated in the tests themselves
		boolean result = false;
		
		if(twg.size() != ag.size()) 
			throw new TestException("Word lister and automaton lister have different sizes!");
		
		Iterator<List<Integer>> twgIt = twg.iterator();
		Iterator<RegisterAutomaton> ragIt = ag.iterator();
		
		//make the tests
		//For each word and automaton, test each algorithm
		while(twgIt.hasNext() && ragIt.hasNext()) {
			RegisterAutomaton a = ragIt.next();
			List<Integer> testWord = twgIt.next();
			
			signalProgression();
			
			System.out.println("Current word size: " + testWord.size() + " symbols");
			
			boolean previousResult = false;
			for(int algIndex = 0; algIndex < algorithms.length; algIndex++) {
				MBSDecisionAlgorithm algorithm = algorithms[algIndex];
				
				//PREPARE AUTOMATON
				algorithm.setAutomaton(a);
				
				//TEST CORE
				long cTime = System.currentTimeMillis();			
				result = algorithm.decide(a, testWord);
				long testTime = System.currentTimeMillis()-cTime;
				
				//Record results
				successMemberships += result ? 1 : 0;
				rc.addSessionNumber(algorithm.name, "Time", (int)testTime); 
				totalTimes[algIndex][twg.getIndex()] = (int) (testTime);
				
				if(algIndex > 0 && previousResult != result)
					throw new TestException("Consistency failure: algorithms disagree on " + testWord.toString());
				else
					previousResult = result;
				
				rc.addSessionNumber(algorithm.name, "Word Size", testWord.size());
				signalProgression();
			}
			
		}
		
		//print all useful data
		for(int algIndex = 0; algIndex < algorithms.length; algIndex++) {
			MBSDecisionAlgorithm algorithm = algorithms[algIndex];
			
			addStats(rc.getSession(algorithm.name));
		}
	}
	
	@Override
	public void customPrint(ResultsContainer rc) {
		rc.println("Total number of success memberships: " + successMemberships + "/" + twg.size());
	}
}
