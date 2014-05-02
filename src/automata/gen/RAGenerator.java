package automata.gen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * The aim of this toolbox is to automatically generate very large automata.
 * Generated automata should be randomised, and can be very large.
 * 
 * Advanced versions could include a vicinity factor, a connectivity check, 
 * initialised registers, better label choice (often it's rho), etc.
 * @author vincent
 *
 */
public class RAGenerator {
	public final static String PATH_ROOT = "res/gen";
	
	//Parameters with default values
	public int averageBranchingFactor = 3;
	
	public int targetNumStates = 1000;
	public int targetNumTransitions = 3000;
	
	public int numRegisters = 20;

	public double definedRhoProportion = 0.9;
	public double labelIsRhoProportion = 0.8;
	/**
	 * The proportion most variables can vary
	 */
	public double slack = 0.3;
	/**
	 * The proportion of expected number of final states
	 */
	public double fProportion = 0.05;
	
	
	//Generation methods
	/**
	 * Generate a basic RA by following the same description process 
	 * as our custom FMA format. Save the automaton in an FMA file.
	 * @return the path to that file.
	 * @throws FileNotFoundException 
	 */
	public String generate() throws FileNotFoundException {
		//Init output file
		String filename = chooseFileName();
		PrintWriter file = new PrintWriter(filename);
		
		//Init registers
		int actualNumRegisters = slackChoose(numRegisters);
		
		//Describe states
		int actualNumStates = slackChoose(targetNumStates);
		int[] rhos = new int[actualNumStates];
		
		for(int s = 0; s < actualNumStates; s++) {
			
			String stateString = "q" + s;
			boolean hasRho = Math.random() < definedRhoProportion;
										
			if(hasRho) {
				//Add rho to the state
				int rho = pickFrom(actualNumRegisters);
				rhos[s] = rho;
				stateString += " " + (rho+1);
			} else {
				rhos[s] = pickFrom(actualNumRegisters);
			}
			
			if(Math.random() < fProportion) {
				//Add F modifier
				if(!hasRho)
					stateString += " _";
				stateString += " F";
			}
			
			file.println(stateString);
		}
		
		//Describe initial state
		file.println("-");
		file.println("q0");
		
		//Describe registers
		for(int r = 0; r < actualNumRegisters-1; r++) {
			file.print("# ");
		}
		file.println("#");
		file.println("-");
		
		//Describe transitions
		int actualNumTransitions = slackChoose(targetNumTransitions);
		
		for(int t = 0; t < actualNumTransitions; t++) {
			//For each transition, choose a random source state
			//and a random destination state, with a random register
			//and a random containing label
			int s1 = pickFrom(actualNumStates);
			int s2 = pickFrom(actualNumStates);
			int label = (Math.random() < labelIsRhoProportion ? 
					rhos[s1] :
					pickFrom(actualNumRegisters))+1;
					
			file.println("q" + s1 + " " + label + " q" + s2);
		}
		
		
		file.close();
		return filename;
	}
	
	private int slackChoose(int target) {
		return (int) ((1.0+2.0*(Math.random()-0.5)*slack)*(double)target);
	}
	
	private int pickFrom(int max) {
		return (int)(Math.random()*(double)max);
	}
	
	private String chooseFileName() {
		int id = 0;
		
		while((new File(PATH_ROOT + id + ".fma")).exists()) {
			id ++;
		}
		
		return PATH_ROOT + id + ".fma";
	}
}
