package testbench;

import java.io.FileNotFoundException;
import java.text.ParseException;

import algorithms.Membership;
import automata.RegisterAutomaton;
import automata.gen.RAGenerator;

public class Testbench {
	public static void main(String[] args) {
		System.out.println("This is testbench, running...");
		
		try {
			RAGenerator rag = new RAGenerator();
			String path = rag.generate();
			
			System.out.println("File written to " + path);
			
			RegisterAutomaton ra = new RegisterAutomaton(path);
			ra.displayInfo();
			
			if(Membership.isMember(ra, new int[] {10,3,1,5,1,6,20,2,50,4,8,9})) {
				System.out.println("Membership test is success");
			} else
				System.out.println("Membership test failed");
		} catch (FileNotFoundException | NumberFormatException | ParseException e) {
			System.out.println("Test failed, here's why:");
			e.printStackTrace();
		}
	}
}
