package testbench;

import java.util.Arrays;
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
	public static final int NUM_TESTED_WORDS = 1000;
	public static final int MIN_LENGTH = 10;
	public static final int MAX_LENGTH = 50;
	
	private final Integer[] minimalAlphabet;
	
	private final int[][] testWords = new int[NUM_TESTED_WORDS][];
	
	//Results
	private final boolean[] results = new boolean[NUM_TESTED_WORDS];
	private final int[][] nodesExpanded = new int[2][NUM_TESTED_WORDS];
	private final int[][] maxFrontierSize = new int[2][NUM_TESTED_WORDS];
	private long bflgsTotalTime = 0L;
	private long ldftsTotalTime = 0L;
	
	public MultiMembershipTest(RegisterAutomaton a) {
		super("Multiple Membership Checks", a);
		
		minimalAlphabet = Tools.computeMinimalAlphabet(a);
		
		maxProgression = NUM_TESTED_WORDS*2; //Two algorithms to test
		progression = 0;
	}

	@Override
	protected void run() throws TestException {
		//Consistency checks are integrated in the tests themselves
		boolean result = false;
		
		//Step 1: choose the words to be tested
		for(int wIndex = 0; wIndex < NUM_TESTED_WORDS; wIndex++) {
			testWords[wIndex] = computeRandomWord();
		}
		
		//Step 2: time on LDFTS nondeterministic membership
		for(int wIndex = 0; wIndex < NUM_TESTED_WORDS; wIndex++) {
			signalProgression();
			
			long cTime = System.currentTimeMillis();			
			result = Membership.ldftsMemberCheck(a, testWords[wIndex]);
			long testTime = System.currentTimeMillis()-cTime;
			ldftsTotalTime += testTime;
			
			results[wIndex] = result;
			
			List<Integer> numbers = rc.getNumbersList();
			if(numbers.size() > 0) {
				nodesExpanded[0][wIndex] = numbers.get(0);
				maxFrontierSize[0][wIndex] = numbers.get(1);
			}
			
			progression++;
		}
		
		//Step 3: time on BFLGS nondeterministic membership
		for(int wIndex = 0; wIndex < NUM_TESTED_WORDS; wIndex++) {
			signalProgression();
			
			long cTime = System.currentTimeMillis();			
			result = Membership.ldftsMemberCheck(a, testWords[wIndex]);
			long testTime = System.currentTimeMillis()-cTime;
			bflgsTotalTime += testTime;
			
			if(result != results[wIndex]) 
				throw new TestException("Consistency failure: algorithms disagree on " + Arrays.toString(testWords[wIndex]));
			
			List<Integer> numbers = rc.getNumbersList();
			if(numbers.size() > 0) {
				nodesExpanded[1][wIndex] = numbers.get(0);
				maxFrontierSize[1][wIndex] = numbers.get(1);
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
		rc.println("BFLGS total execution time:   " + prettyPrintMillis(bflgsTotalTime));
		rc.println("BFLGS average execution time: " + prettyPrintMillis(bflgsTotalTime/NUM_TESTED_WORDS));
		rc.println("");
		rc.println("Total number of success memberships: " + successMemberships + "/" + NUM_TESTED_WORDS);
	}

}
