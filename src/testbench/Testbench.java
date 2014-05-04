package testbench;

import java.io.FileNotFoundException;
import java.text.ParseException;

import algorithms.Emptiness;
import algorithms.Membership;
import algorithms.emptiness.WordIterator;
import algorithms.tools.ResultsContainer;
import automata.RegisterAutomaton;
import automata.gen.RAGenerator;

public class Testbench {
	public static void main(String[] args) {
		System.out.println("This is testbench, running...");
		
		try {
			RAGenerator rag = new RAGenerator();
			String path = rag.generate();
			
			System.out.println("File written to " + path);
			
			RegisterAutomaton ra = new RegisterAutomaton("res/example3.fma");
			//RegisterAutomaton ra = new RegisterAutomaton(path);
			ra.displayInfo();
			
			if(Membership.isMember(ra, new int[] {10,3,1,5,1,6,20,2,50,4,8,9})) {
				System.out.println("Membership test is success");
				ResultsContainer.getContainer().flush();
			} else
				System.out.println("Membership test failed");
			
			if(Emptiness.empiricalEmptinessCheck(ra, 10)) {
				System.out.println("Emptiness check found a word that was accepted");
				ResultsContainer.getContainer().flush();
			} else {
				System.out.println("Empirical emptiness check was not successful");
			}
		} catch (FileNotFoundException | NumberFormatException | ParseException e) {
			System.out.println("Test failed, here's why:");
			e.printStackTrace();
		}
		
		//Print results to standard output
	}
}
