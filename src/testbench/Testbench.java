package testbench;

import java.io.FileNotFoundException;
import java.text.ParseException;

import algorithms.Emptiness;
import algorithms.Membership;
import algorithms.tools.ResultsContainer;
import automata.RegisterAutomaton;
import automata.gen.AutomatonGenerator;
import automata.gen.RandomGenerator;

public class Testbench {
	public static void main(String[] args) {
		System.out.println("This is testbench, running...");
		
		try {
			AutomatonGenerator rag = new RandomGenerator();
			String path = rag.generate();
			
			System.out.println("File written to " + path);
			
			RegisterAutomaton ra = new RegisterAutomaton("res/example3.fma");
			//RegisterAutomaton ra = new RegisterAutomaton(path);
			ra.displayInfo();
			
			if(Membership.isMember(ra, new int[] {10,3,1,5,1,6,20,2,20,4,8,9})) {
				System.out.println("Membership test is success");
				ResultsContainer.getContainer().flush();
			} else
				System.out.println("Membership test failed");
			
			if(Emptiness.generativeCompleteEmptinessCheck(ra)) {
				System.out.println("Emptiness check found a word that was accepted");
				ResultsContainer.getContainer().flush();
			} else {
				System.out.println("Automaton is empty");
			}
		} catch (FileNotFoundException | NumberFormatException | ParseException e) {
			System.out.println("Test failed, here's why:");
			e.printStackTrace();
		}
		
		//Print results to standard output
	}
}
