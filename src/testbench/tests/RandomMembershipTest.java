package testbench.tests;

import java.util.ArrayList;
import java.util.List;

import testbench.Test;
import testbench.TestException;
import testbench.lister.TestLister;
import algorithms.Tools;
import algorithms.membership.MBSDecisionAlgorithm;
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
	
	private final RegisterAutomaton a;
	private final MBSDecisionAlgorithm[] algorithms;
	private Integer[] minimalAlphabet;
	private TestLister<List<Integer>> twg;
	private ListMembershipTest lmt;
	
	//methods
	public RandomMembershipTest(RegisterAutomaton a, 
								MBSDecisionAlgorithm[] algorithms) {
		super("Random Membership Checks", a);
		this.a = a;
		this.algorithms = algorithms;
		
		maxProgression = 1; //Two algorithms to test
	}

	@Override
	protected void run() throws TestException {
		signalProgression();
		lmt.test();
		signalProgression();
	}
	
	private List<Integer> computeRandomWord() {
		int length = pickFrom(MAX_LENGTH-MIN_LENGTH) + MIN_LENGTH;
		List<Integer> word = new ArrayList<>(length);
		
		for(int sIndex = 0; sIndex < length; sIndex++) {
			word.add(minimalAlphabet[pickFrom(minimalAlphabet.length)]+1);
		}
		
		return word;
	}
	
	@Override
	protected void prepare() {
		minimalAlphabet = Tools.computeMinimalAlphabet(a);
		
		twg = new TestLister<List<Integer>>() {
			
			@Override
			public int size() {
				return NUM_TESTED_WORDS;
			}
			
			@Override
			protected List<Integer> nextResource() {
				return computeRandomWord();
			}
		};
		
		lmt = new ListMembershipTest(a, algorithms, twg);
	}

}
