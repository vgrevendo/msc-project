package testbench;

import java.io.FileNotFoundException;
import java.text.ParseException;

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
		} catch (FileNotFoundException | ParseException e) {
			System.out.println("An error occurred:");
			e.printStackTrace();
		}
	}
}
