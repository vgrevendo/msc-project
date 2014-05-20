package testbench;

import java.io.FileNotFoundException;
import java.text.ParseException;

import testbench.lister.TestLister;
import testbench.tests.AsymptoticMembershipTest;
import testbench.tests.ListMembershipTest;
import algorithms.Membership;
import algorithms.membership.MBSDecisionAlgorithm;
import algorithms.tools.ResultsContainer;
import automata.RegisterAutomaton;
import automata.gen.AutomatonGenerator;
import automata.gen.RootBranchGenerator;
import automata.hra.HRAutomaton;

public class Testbench {
	public final static int TEST_LENGTH = 1500;
	
	public static void main(String[] args) {
		System.out.println("This is testbench, running...");
		
		try {
			mbsAsymptTests();
			
			
		} catch (FileNotFoundException | ParseException e) {
			System.out.println("An error occurred:");
			e.printStackTrace();
		}
	}
	
	public static void mbsTests() throws FileNotFoundException, ParseException {
		MBSDecisionAlgorithm[] algorithms = new MBSDecisionAlgorithm[] {
				//Membership.ldftsCheck,
				Membership.bflgsCheck,
				Membership.bestFirstCheck};

		RegisterAutomaton ra = new HRAutomaton("res/example3.fma", TEST_LENGTH);
		ra.displayInfo();
		
		TestLister<int[]> twg = new TestLister<int[]>() {
			@Override
			public int size() {
				return TEST_LENGTH-7;
			}
			
			@Override
			protected int[] nextResource() {
				return new int[index+5];
			}
		};
		
		Test lmt = new ListMembershipTest(ra, algorithms, twg);
		lmt.test();
		
		ResultsContainer.getContainer().flush();
	}
	
	/**
	 * Tests for membership on sequences of (automaton, word)
	 * @throws FileNotFoundException
	 * @throws ParseException
	 */
	public static void mbsAsymptTests() throws FileNotFoundException, ParseException {
		MBSDecisionAlgorithm[] algorithms = new MBSDecisionAlgorithm[] {
				Membership.ldftsCheck,
				Membership.bflgsCheck,
				Membership.bestFirstCheck,
				Membership.aStarCheck};
		
		final int numWords = 100;

		TestLister<int[]> twg = new TestLister<int[]>() {
			@Override
			public int size() {
				return numWords;
			}
			
			@Override
			protected int[] nextResource() {
				return new int[index+5];
			}
		};
		
		TestLister<RegisterAutomaton> rag = new TestLister<RegisterAutomaton>() {
			@Override
			protected RegisterAutomaton nextResource() {
				RootBranchGenerator rbg = new RootBranchGenerator(index+5);
				String filename;
				try {
					filename = rbg.generate();
					return new HRAutomaton(filename, index+10);
				} catch (ParseException | FileNotFoundException e) {
					e.printStackTrace();
				}
				
				return null;
				
			}

			@Override
			public int size() {
				return numWords;
			}
		};
		
		Test amt = new AsymptoticMembershipTest(rag, twg, algorithms);
		amt.test();
		
		ResultsContainer.getContainer().flush();
	}
	
	public static void generatorTests() throws FileNotFoundException, ParseException {
		System.out.println("This is GENERATOR TESTBENCH");
		
		AutomatonGenerator ag = new RootBranchGenerator(5);
		String filename = ag.generate();
		
		RegisterAutomaton ra = new RegisterAutomaton(filename);
		ra.displayInfo();
	}
}
