package testbench;

import java.io.FileNotFoundException;
import java.text.ParseException;

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
			generatorTests();
			
			
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
		
		TestWordGenerator twg = new TestWordGenerator() {
			@Override
			public int size() {
				return TEST_LENGTH-7;
			}
			
			@Override
			protected int[] nextWord() {
				return new int[wordIndex+5];
			}
		};
		
		Test lmt = new ListMembershipTest(ra, algorithms, twg);
		lmt.test();
		
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
