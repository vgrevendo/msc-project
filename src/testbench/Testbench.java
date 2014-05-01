package testbench;

import java.io.FileNotFoundException;
import java.text.ParseException;

import algorithms.Membership;
import automata.RegisterAutomaton;

public class Testbench {
	public static void main(String[] args) {
		System.out.println("This is testbench, running...");
		
		try {
			RegisterAutomaton ra = new RegisterAutomaton("res/example2.fma");
			ra.displayInfo();
			
			if(Membership.isMember(ra, new int[] {1,2,1,1,2,1})) {
				System.out.println("Membership test is success");
			} else
				System.out.println("Membership test failed");
		} catch (FileNotFoundException | ParseException | NumberFormatException e) {
			System.out.println("Automaton could not be loaded, here's why:");
			e.printStackTrace();
		}
	}
}
