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
public class RandomMembershipTest extends Test {
	public static final int NUM_TESTED_WORDS = 1000;
	public static final int MIN_LENGTH = 10;
	public static final int MAX_LENGTH = 20;
	
	private final Integer[] minimalAlphabet;
	
	private final int[][] testWords = new int[NUM_TESTED_WORDS][];
	
	//Results
	private final boolean[] results = new boolean[NUM_TESTED_WORDS];
	private final int[][] nodesExpanded = new int[2][NUM_TESTED_WORDS];
	private final int[][] maxFrontierSize = new int[2][NUM_TESTED_WORDS];
	private long bflgsTotalTime = 0L;
	private long ldftsTotalTime = 0L;
	
	public RandomMembershipTest(RegisterAutomaton a) {
		super("Multiple Membership Checks", a);
		
		minimalAlphabet = Tools.computeMinimalAlphabet(a);
		
		maxProgression = NUM_TESTED_WORDS*2; //Two algorithms to test
	}

	@Override
	protected void run() throws TestException {
		//Consistency checks are integrated in the tests themselves
		boolean result = false;
		
		//Step 1: choose the words to be tested
		for(int wIndex = 0; wIndex < NUM_TESTED_WORDS; wIndex++) {
			testWords[wIndex] = computeRandomWord();
		}
		
		signalProgression();
		//Step 2: time on LDFTS nondeterministic membership
		for(int wIndex = 0; wIndex < NUM_TESTED_WORDS; wIndex++) {
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
			signalProgression();
		}
		
		//Step 3: time on BFLGS nondeterministic membership
		for(int wIndex = 0; wIndex < NUM_TESTED_WORDS; wIndex++) {
			long cTime = System.currentTimeMillis();			
			result = Membership.bflgsMemberCheck(a, testWords[wIndex]);
			long testTime = System.currentTimeMillis()-cTime;
			bflgsTotalTime += testTime;
			
			if(result != results[wIndex]) 
				throw new TestException("Consistency failure: algorithms disagree on " + Arrays.toString(testWords[wIndex]));
			
			List<Integer> numbers = rc.getNumbersList();
			if(numbers.size() > 0) {
				nodesExpanded[1][wIndex] = numbers.get(0);
				maxFrontierSize[1][wIndex] = numbers.get(1);
			}
			
			signalProgression();
		}
		
		//Step 4: make sure all useful data is printed
		addCsvColumn(nodesExpanded[0], "LDFTS nodes");
		addCsvColumn(nodesExpanded[1], "BFLGS nodes");
		addCsvColumn(maxFrontierSize[0], "LDFTS frontier");
		addCsvColumn(maxFrontierSize[1], "BFLGS frontier");
		
		String[] testWordStrings = new String[NUM_TESTED_WORDS];
		for(int i = 0; i < NUM_TESTED_WORDS; i++)
			testWordStrings[i] = Arrays.toString(testWords[i]);
		
		addCsvColumn(testWordStrings, "Test word");
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
