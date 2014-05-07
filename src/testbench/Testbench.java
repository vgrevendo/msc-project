package testbench;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Arrays;

import algorithms.Membership;
import algorithms.tools.ResultsContainer;
import automata.RegisterAutomaton;
import automata.gen.AutomatonGenerator;
import automata.gen.DiamondChainGenerator;
import automata.gen.RandomGenerator;

public class Testbench {
	public static void main(String[] args) {
		System.out.println("This is testbench, running...");
		
		try {
			AutomatonGenerator ag = new RandomGenerator();
			String path = ag.generate();
			RegisterAutomaton ra = new RegisterAutomaton(path);
			ra.displayInfo();
			
			Test mmt = new MultiMembershipTest(ra);
			mmt.test();
			
			ResultsContainer.getContainer().flush();
			
//			RegisterAutomaton ra = new RegisterAutomaton("res/example3.fma");
//			ra.displayInfo();
//			
//			if(Membership.bflgsMemberCheck(ra, new int[]{1,1,1,1,2,3,6,8,4,7,9})) {
//				System.out.println("Success");
//			} else {
//				System.out.println("Failure");
//			}
			
//			int[] results = new int[10];
//			
//			for(int i = 0; i < 100; i++) {
//				results[(int) (Math.random()*(results.length))]++;
//			}
//			
//			System.out.println(Arrays.toString(results));
		} catch (FileNotFoundException | ParseException e) {
			System.out.println("An error occurred:");
			e.printStackTrace();
		}
	}
}
