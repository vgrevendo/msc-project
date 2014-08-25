package testbench.tests;

import java.util.Iterator;

import algorithms.emptiness.EMPDecisionAlgorithm;
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
public class AsymptoticEmptinessTest extends Test {
	private final TestLister<RegisterAutomaton> ag;
	private final EMPDecisionAlgorithm[] algorithms;
	
	//Results
	private final int[][] totalTimes;
	private int successMemberships = 0;

	public AsymptoticEmptinessTest(TestLister<RegisterAutomaton> ag, 
									EMPDecisionAlgorithm[] algorithms) {
		//We don't give an automaton to the super class, because we don't need any
		super("Asymptotic MBS", null);
		this.algorithms = algorithms;
		this.ag = ag;
		
		maxProgression = ag.size()*algorithms.length;
		totalTimes = new int[algorithms.length][ag.size()];
	}

	@Override
	protected void run() throws TestException {
		//Consistency checks are integrated in the tests themselves
		boolean result = false;
		
		Iterator<RegisterAutomaton> ragIt = ag.iterator();
		
		signalProgression();
		//make the tests
		//For each word and automaton, test each algorithm
		while(ragIt.hasNext()) {
			RegisterAutomaton a = ragIt.next();
			
			System.out.println("Current automaton:");
			a.displayInfo();
			
			boolean previousResult = false;
			for(int algIndex = 0; algIndex < algorithms.length; algIndex++) {
				EMPDecisionAlgorithm algorithm = algorithms[algIndex];
				
				//TEST CORE
				long cTime = System.currentTimeMillis();			
				result = algorithm.decide(a);
				long testTime = System.currentTimeMillis()-cTime;
				
				//Record results
				successMemberships += result ? 1 : 0;
				rc.addSessionNumber(algorithm.name, "Time", (int)testTime); 
				totalTimes[algIndex][ag.getIndex()] = (int) (testTime);
				
				if(algIndex > 0 && previousResult != result)
					throw new TestException("Consistency failure: algorithms disagree on this emptiness check!");
				else
					previousResult = result;
				
				rc.addSessionNumber(algorithm.name, "Test n", ag.getIndex());
				rc.addSessionNumber(algorithm.name, "|Q|", a.getStates().length);
				rc.addSessionNumber(algorithm.name, "R", a.getInitialRegisters().length);
				rc.addSessionNumber(algorithm.name, "|mu|", a.countTransitions());
				signalProgression(true);
			}
			
		}
		
		//print all useful data
		for(int algIndex = 0; algIndex < algorithms.length; algIndex++) {
			EMPDecisionAlgorithm algorithm = algorithms[algIndex];
			
			addStats(rc.getSession(algorithm.name));
		}
	}
	
	@Override
	public void customPrint(ResultsContainer rc) {
		rc.println("Total number of nonempty automata: " + successMemberships + "/" + ag.size());
	}
}
