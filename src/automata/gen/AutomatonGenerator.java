package automata.gen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * The aim of this toolbox is to automatically generate very large automata.
 * Generated automata should be randomised or chosen intelligently, and can 
 * be very large.
 * 
 * Advanced versions could include a vicinity factor, a connectivity check, 
 * initialised registers, better label choice (often it's rho), etc.
 * @author vincent
 *
 */
public abstract class AutomatonGenerator {
	public final static String PATH_ROOT = "res/gen";
	
	//Generic parameters
	/**
	 * The proportion most variables can vary
	 */
	protected double slack = 0.3;
	
	//Generic file building
	private final List<String> transitionLines;
	private final List<String> stateLines;
	private final List<String> commentLines;
	private int initialState = 0;
	private StringBuilder registersSb = new StringBuilder();  
	
	public AutomatonGenerator(String name) {
		stateLines = new ArrayList<>();
		transitionLines = new ArrayList<>();
		commentLines = new ArrayList<>();
		
		String date = "";
		
		commentLines.add("-- Generated on " + date + " by " + name);
	}
	
	//Generation methods
	/**
	 * This method should be implemented by the subclass, which
	 * should make use of building methods like addState and
	 * addTransition
	 */
	protected abstract void build();
	
	/**
	 * Generate an automaton as defined by the subclass
	 * @return
	 * @throws FileNotFoundException 
	 */
	public String generate() throws FileNotFoundException {
		build();
		return produce();
	}
	
	//Generic tools	
	protected int slackChoose(int target) {
		return (int) ((1.0+2.0*(Math.random()-0.5)*slack)*(double)target);
	}
	
	protected int pickFrom(int max) {
		return (int)(Math.random()*(double)max);
	}
	
	private String chooseFileName() {
		int id = 0;
		
		while((new File(PATH_ROOT + id + ".fma")).exists()) {
			id ++;
		}
		
		return PATH_ROOT + id + ".fma";
	}
	
	protected void addTransition(int s1, int s2, int label) {
		transitionLines.add("q" + s1 + " " + (label+1) + " q" + s2);
	}
	
	protected void addState(int number, int rho, boolean isFinal) {
		String stateString = "q" + number;
		
		if(rho < 0 && isFinal) {
			stateString += " _ F";
		} else if(rho >= 0 && !isFinal) {
			stateString += " " + (rho+1);
		} else if(rho >= 0 && isFinal) {
			stateString += " " + (rho+1) + " F";
		}
		
		stateLines.add(stateString);
	}
	
	protected void setInitialState(int initialState) {
		this.initialState = initialState;
	}

	protected void addRegister(int initSymbol) {
		if(registersSb.length() > 0)
			registersSb.append(" ");
		
		registersSb.append(initSymbol < 0 ? "#" : Integer.toString(initSymbol));
	}
	
	private String produce() throws FileNotFoundException {
		//Init output file
		String filename = chooseFileName();
		PrintWriter file = new PrintWriter(filename);
		
		//Describe states
		for(String stateString : stateLines) {
			file.println(stateString);
		}
		
		//Describe initial state and registers
		file.println("-");
		file.println("q" + initialState);
		file.println(registersSb.toString());
		file.println("-");
		
		//Describe transitions
		for(String transitionString : transitionLines) {
			file.println(transitionString);
		}
		
		file.close();
		
		//Prepare for new generation
		initialState = 0;
		registersSb = new StringBuilder();
		transitionLines.clear();
		stateLines.clear();
		
		System.out.println("Automaton output to " + filename);
		
		return filename;
	}
}
