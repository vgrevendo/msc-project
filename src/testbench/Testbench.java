package testbench;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import algorithms.Membership;
import algorithms.tools.ResultsContainer;
import automata.RegisterAutomaton;
import automata.gen.AutomatonGenerator;
import automata.gen.DiamondChainGenerator;
import automata.gen.RandomGenerator;
import automata.gen.StrictDCGenerator;

public class Testbench {
	public static void main(String[] args) {
		System.out.println("This is testbench, running...");
		
		try {
//			AutomatonGenerator ag = new DiamondChainGenerator();
//			String path = ag.generate();
//			RegisterAutomaton ra = new RegisterAutomaton(path);
//			ra.displayInfo();
//			
//			Test mmt = new MultiMembershipTest(ra);
//			mmt.test();
//			
//			ResultsContainer.getContainer().flush();
			
//			final int length = 50;
//			
			RegisterAutomaton ra = new RegisterAutomaton("res/example5.fma");
			ra.displayInfo();
			
			Test lmt = new ListedMembershipTest(ra, new TestWordGenerator() {
				@Override
				public int size() {
					return 250;
				}
				
				@Override
				protected int[] nextWord() {
					return new int[wordIndex+5];
				}
			});
			lmt.test();
			ResultsContainer.getContainer().flush();
//			
//			if(Membership.bflgsMemberCheck(ra, new int[length])) {
//				System.out.println("Success");
//			} else {
//				System.out.println("Failure");
//			}
//			
//			ResultsContainer rc = ResultsContainer.getContainer();
//			List<Integer> numbersList = rc.getNumbersList();
//			if(numbersList != null && !numbersList.isEmpty()) {
//				System.out.println("BFLGS nodes:    " + numbersList.get(0));
//				System.out.println("BFLGS frontier: " + numbersList.get(1));
//			}
//			
//			if(Membership.ldftsMemberCheck(ra, new int[length])) {
//				System.out.println("Success");
//			} else {
//				System.out.println("Failure");
//			}
//			
//			numbersList = rc.getNumbersList();
//			if(numbersList != null && !numbersList.isEmpty()) {
//				System.out.println("LDFTS nodes:    " + numbersList.get(0));
//				System.out.println("LDFTS frontier: " + numbersList.get(1));
//			}
			
//			int[] results = new int[10];
//			
//			for(int i = 0; i < 100; i++) {
//				results[(int) (Math.random()*(results.length))]++;
//			}
//			
//			System.out.println(Arrays.toString(results));
			
//			AutomatonGenerator ag = new StrictDCGenerator();
//			String fname = ag.generate();
//			RegisterAutomaton ra = new RegisterAutomaton(fname);
//			ra.displayInfo();
//			
//			Test mmt = new MultiMembershipTest(ra);
//			mmt.test();
//			
//			ResultsContainer.getContainer().flush();
		} catch (FileNotFoundException | ParseException e) {
			System.out.println("An error occurred:");
			e.printStackTrace();
		}
	}
}
