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
	private final boolean[] results;
	private final int[][] nodesExpanded;
	private final int[][] maxFrontierSize;
	private final int[][] totalTimes;
	private final int[][] runTimes;
	private final int[] ns;

	public AsymptoticMembershipTest(TestLister<RegisterAutomaton> ag, 
									TestLister<List<Integer>> twg,
									MBSDecisionAlgorithm[] algorithms) {
		//We don't give an automaton to the super class, because we don't need any
		super("Asymptotic MBS", null);
		this.algorithms = algorithms;
		this.ag = ag;
		this.twg = twg;
		
		maxProgression = twg.size()*algorithms.length;
		
		results = new boolean[twg.size()];
		nodesExpanded = new int[algorithms.length][twg.size()];
		maxFrontierSize = new int[algorithms.length][twg.size()];
		totalTimes = new int[algorithms.length][twg.size()];
		runTimes = new int[algorithms.length][twg.size()];
		ns = new int[twg.size()];
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
			
			for(int algIndex = 0; algIndex < algorithms.length; algIndex++) {
				signalProgression();
				
				MBSDecisionAlgorithm algorithm = algorithms[algIndex];
				
				//PREPARE AUTOMATON
				long cTime = System.currentTimeMillis();			
				algorithm.setAutomaton(a);
				long prepTime = System.currentTimeMillis()-cTime;
				
				
				//TEST CORE
				cTime = System.currentTimeMillis();			
				result = algorithm.decide(a, testWord);
				long testTime = System.currentTimeMillis()-cTime;
				
				//Record results
				totalTimes[algIndex][twg.getIndex()] = (int) (prepTime + testTime);
				runTimes[algIndex][twg.getIndex()] = (int) testTime;
				
				if(algIndex > 0 && results[twg.getIndex()] != result)
					throw new TestException("Consistency failure: algorithms disagree on " + testWord.toString());
				else
					results[twg.getIndex()] = result;
				
				List<Integer> numbers = null;
				if(numbers.size() > 0) {
					nodesExpanded[algIndex][twg.getIndex()] = numbers.get(0);
					maxFrontierSize[algIndex][twg.getIndex()] = numbers.get(1);
				}
			}
			
			ns[twg.getIndex()] = testWord.size();
		}
		
		signalProgression();
		
		
		//print all useful data
		addCsvColumn(ns, "Input size");
		for(int algIndex = 0; algIndex < algorithms.length; algIndex++) {
			MBSDecisionAlgorithm algorithm = algorithms[algIndex];
			
			addCsvColumn(nodesExpanded[algIndex], algorithm + " nodes");
			addCsvColumn(maxFrontierSize[algIndex], algorithm + " frontier");
			addCsvColumn(totalTimes[algIndex], algorithm + " time");
			addCsvColumn(runTimes[algIndex], algorithm + " runtime");
		}
	}
	
	@Override
	public void customPrint(ResultsContainer rc) {
		int successMemberships = 0;
		for(boolean b: results) {
			successMemberships += b ? 1 : 0;
		}
		
		rc.println("Total number of success memberships: " + successMemberships + "/" + twg.size());
	}

	//No preparation is necessary since automata 
	//need to be prepared again before each run...
}
