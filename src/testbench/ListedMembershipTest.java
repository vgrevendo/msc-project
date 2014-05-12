package testbench;

import java.util.Arrays;
import java.util.List;

import algorithms.Membership;
import algorithms.tools.ResultsContainer;
import automata.RegisterAutomaton;

/**
 * A multiple membership test, comparing several algorithms (LDFTS, BFLGS, ...).
 * Take a lot of words of various lengths and compare performances. 
 * @author vincent
 */
public class ListedMembershipTest extends Test {
	public static final int MIN_LENGTH = 10;
	public static final int MAX_LENGTH = 20;
	
	private final TestWordGenerator twg;
	
	//Results
	private final boolean[] results;
	private final int[][] nodesExpanded;
	private final int[][] maxFrontierSize;
	private final int[][] times;
	private long bflgsTotalTime = 0L;
	private long ldftsTotalTime = 0L;
	
	public ListedMembershipTest(RegisterAutomaton a, TestWordGenerator twg) {
		super("Listed Membership Checks", a);
		this.twg = twg;
		
		maxProgression = twg.size()*2; //Two algorithms to test
		
		results = new boolean[twg.size()];
		nodesExpanded = new int[2][twg.size()];
		maxFrontierSize = new int[2][twg.size()];
		times = new int[2][twg.size()];
	}

	@Override
	protected void run() throws TestException {
		//Consistency checks are integrated in the tests themselves
		boolean result = false;
		String[] testWordStrings = new String[twg.size()];
		
		//make the tests
		for(int[] testWord : twg) {
			signalProgression();
			
			//LDFTS
			long cTime = System.currentTimeMillis();			
			result = Membership.ldftsMemberCheck(a, testWord);
			long testTime = System.currentTimeMillis()-cTime;
			ldftsTotalTime += testTime;
			
			times[0][twg.getWordIndex()] = (int) testTime;
			results[twg.getWordIndex()] = result;
			
			List<Integer> numbers = rc.getNumbersList();
			if(numbers.size() > 0) {
				nodesExpanded[0][twg.getWordIndex()] = numbers.get(0);
				maxFrontierSize[0][twg.getWordIndex()] = numbers.get(1);
			}
			
			signalProgression();

			//BFLGS
			cTime = System.currentTimeMillis();			
			result = Membership.bflgsMemberCheck(a, testWord);
			testTime = System.currentTimeMillis()-cTime;
			bflgsTotalTime += testTime;
			
			times[1][twg.getWordIndex()] = (int) testTime;
			if(result != results[twg.getWordIndex()]) 
				throw new TestException("Consistency failure: algorithms disagree on " + Arrays.toString(testWord));
			
			numbers = rc.getNumbersList();
			if(numbers.size() > 0) {
				nodesExpanded[1][twg.getWordIndex()] = numbers.get(0);
				maxFrontierSize[1][twg.getWordIndex()] = numbers.get(1);
			}
			
			testWordStrings[twg.getWordIndex()] = Arrays.toString(testWord);
			
		}
		
		signalProgression();
		
		
		//Step 4: make sure all useful data is printed
		addCsvColumn(nodesExpanded[0], "LDFTS nodes");
		addCsvColumn(nodesExpanded[1], "BFLGS nodes");
		addCsvColumn(maxFrontierSize[0], "LDFTS frontier");
		addCsvColumn(maxFrontierSize[1], "BFLGS frontier");
		addCsvColumn(times[0], "LDFTS time");
		addCsvColumn(times[1], "BFLGS time");
		addCsvColumn(testWordStrings, "Test word");
	}
	
	@Override
	public void customPrint(ResultsContainer rc) {
		int successMemberships = 0;
		for(boolean b: results) {
			successMemberships += b ? 1 : 0;
		}
		
		rc.println("LDFTS total execution time:   " + prettyPrintMillis(ldftsTotalTime));
		rc.println("LDFTS average execution time: " + prettyPrintMillis(ldftsTotalTime/twg.size()));
		rc.println("BFLGS total execution time:   " + prettyPrintMillis(bflgsTotalTime));
		rc.println("BFLGS average execution time: " + prettyPrintMillis(bflgsTotalTime/twg.size()));
		rc.println("");
		rc.println("Total number of success memberships: " + successMemberships + "/" + twg.size());
	}

}
