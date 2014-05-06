package testbench;

import java.util.List;

import algorithms.Membership;
import algorithms.Tools;
import algorithms.tools.ResultsContainer;
import automata.RegisterAutomaton;

/**
 * A multiple membership test, comparing several algorithms (LDFTS, BFLGS, ...).
 * Take a lot of words of various lengths and compare performances. 
 * @author vincent
 */
public class MultiMembershipTest extends Test {
	public static final int NUM_TESTED_WORDS = 10000;
	public static final int MIN_LENGTH = 1;
	public static final int MAX_LENGTH = 50;
	
	private final Integer[] minimalAlphabet;
	
	private final int[][] testWords = new int[NUM_TESTED_WORDS][];
	
	//Results
	private final boolean[] results = new boolean[NUM_TESTED_WORDS];
	private final int[] nodesExpanded = new int[NUM_TESTED_WORDS];
	private final int[] maxFrontierSize = new int[NUM_TESTED_WORDS];
	private long ldftsTotalTime = 0L;
	
	public MultiMembershipTest(RegisterAutomaton a) {
		super("Multiple Membership Checks", a);
		
		minimalAlphabet = Tools.computeMinimalAlphabet(a);
		
		maxProgression = NUM_TESTED_WORDS;
		progression = 0;
	}

	@Override
	protected void run() {
		//Step 1: choose the words to be tested
		for(int wIndex = 0; wIndex < NUM_TESTED_WORDS; wIndex++) {
			testWords[wIndex] = computeRandomWord();
		}
		
		//Step 2: time on LDFTS nondeterministic membership
		for(int wIndex = 0; wIndex < NUM_TESTED_WORDS; wIndex++) {
			signalProgression();
			
			long cTime = System.currentTimeMillis();			
			results[wIndex] = Membership.nondeterministicMemberCheck(a, testWords[wIndex]);
			long testTime = System.currentTimeMillis()-cTime;
			ldftsTotalTime += testTime;
			
			List<Integer> numbers = rc.getNumbersList();
			if(numbers.size() > 0) {
				nodesExpanded[wIndex] = numbers.get(0);
				maxFrontierSize[wIndex] = numbers.get(1);
			}
			
			progression++;
		}
	}
	
	private int[] computeRandomWord() {
		int length = pickFrom(MAX_LENGTH-MIN_LENGTH) + MIN_LENGTH;
		int[] word = new int[length];
		
		for(int sIndex = 0; sIndex < length; sIndex++) {
			word[sIndex] = minimalAlphabet[pickFrom(minimalAlphabet.length)]+1;
		}
		
		return word;
	}
	
	@Override
	public void customPrint(ResultsContainer rc) {
		int successMemberships = 0;
		for(boolean b: results) {
			successMemberships += b ? 1 : 0;
		}
		
		rc.println("LDFTS total execution time:   " + prettyPrintMillis(ldftsTotalTime));
		rc.println("LDFTS average execution time: " + prettyPrintMillis(ldftsTotalTime/NUM_TESTED_WORDS));
		rc.println("");
		rc.println("Total number of success memberships: " + successMemberships + "/" + NUM_TESTED_WORDS);
	}

}
