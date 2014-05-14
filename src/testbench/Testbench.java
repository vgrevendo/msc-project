package testbench;

import java.io.FileNotFoundException;
import java.text.ParseException;

import algorithms.MBSDecisionAlgorithm;
import algorithms.Membership;
import algorithms.tools.ResultsContainer;
import automata.RegisterAutomaton;
import automata.hra.HRAutomaton;

public class Testbench {
	public final static int TEST_LENGTH = 1500;
	
	public static void main(String[] args) {
		System.out.println("This is testbench, running...");
		
		try {
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
			
			Test lmt = new ListedMembershipTest(ra, algorithms, twg);
			lmt.test();
			
			ResultsContainer.getContainer().flush();
		} catch (FileNotFoundException | ParseException e) {
			System.out.println("An error occurred:");
			e.printStackTrace();
		}
	}
}
