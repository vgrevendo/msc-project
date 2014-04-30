package automata;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * A register automaton as defined in "Finite-memory Automata", Kaminski, Francez
 * @author vincent
 *
 */
public class RegisterAutomaton extends Automaton {
	
	private State[] states;
	private State initialState;
	private int[] registers;
	/**
	 * Instead of a function, rho is a relation as well.
	 * This avoids creating function objects.
	 */
	private Map<State, Integer> rho;
	/**
	 * Mu is the relation as defined in the paper.
	 * Nullpointers will have to be handled intelligently here.
	 */
	private Map<State, Map<Integer, State>> mu;
	
	public RegisterAutomaton(String loadPath) throws FileNotFoundException, ParseException {
		loadFromFile(loadPath);
	}
	
	
	//Load from files
	
	private void loadFromFile(String path) throws FileNotFoundException, ParseException {
		//Check file correctness
		Scanner sc = new Scanner(new File(path));
		
		//Initialise automaton components
		this.rho = new HashMap<>();
		this.mu  = new HashMap<>();
		
		//Load components
		int lineNumber = 0;
		lineNumber = loadStates(sc);
		lineNumber = loadRegisters(sc, lineNumber);
		loadTransitions(sc, lineNumber);
	}
	
	private int loadStates(Scanner sc) throws ParseException {
		int lineNumber = 0;
		ArrayList<State> stateSet = new ArrayList<>();
		
		//Read from file and fill out the blanks
		//States
		while(sc.hasNext()) {
			String[] tokens = sc.nextLine().split(" ");
			lineNumber++;
			
			switch(tokens.length) {
			case 1:
				if(tokens[0].equals("-")) {
					//Set the state set
					if(stateSet.size() == 0)
						throw new ParseException("No states found in automaton description", lineNumber);
					states = new State[stateSet.size()];
					stateSet.toArray(states);
					
					//Init mu array
					for(State state : states) {
						mu.put(state, new HashMap<Integer, State>());
					}
					
					return lineNumber;
				} else {
					throw new ParseException("Unrecognised single token", lineNumber);
				}
			case 2:
				State state = new State(tokens[0]);
				stateSet.add(state);
				rho.put(state, Integer.parseInt(tokens[1]));
				break;
			case 3:
				State finalState = new State(tokens[0], tokens[2].equals("F"));
				stateSet.add(finalState);
				rho.put(finalState, Integer.parseInt(tokens[1]));
				break;
			default:
				throw new ParseException("Unrecognised line", lineNumber);
			}
		}
		
		throw new ParseException("File incomplete", lineNumber);
		
	}

	private int loadRegisters(Scanner sc, int lineNumber) throws ParseException {
		if(sc.hasNext()) {
			String[] tokens = sc.nextLine().split(" ");
			registers = new int[tokens.length];
			
			for(int i = 0; i < tokens.length; i++) {
				if(tokens[i].equals("#"))
					registers[i] = -1;
				else				
					registers[i] = Integer.parseInt(tokens[i]);
			}
			
			sc.nextLine();
			return lineNumber+2;
		} else {
			throw new ParseException("Incomplete source file: no registers", lineNumber);
		}
	}
	
	private int loadTransitions(Scanner sc, int lineNumber) throws ParseException {
		//Read from file and fill out the blanks
		//States
		while(sc.hasNext()) {
			String[] tokens = sc.nextLine().split(" ");
			lineNumber++;
			
			if(tokens.length != 3) 
				throw new ParseException("Expected three tokens", lineNumber);
			
			//Lookup states
			State state1 = null;
			State state2 = null;
			
			for(State state : states) {
				if(state.name.equals(tokens[0])) {
					state1 = state;
					break;
				}
			}
			for(State state : states) {
				if(state.name.equals(tokens[2])) {
					state2 = state;
					break;
				}
			}
			
			if(state1 == null || state2 == null) {
				throw new ParseException("Could not resolve reference to states " + tokens[0] + ", " + tokens[2], lineNumber);
			}
			
			mu.get(state1).put(Integer.parseInt(tokens[1]), state2);
		}
		
		return lineNumber;
	}

	public void displayInfo() {
		System.out.println("-- Automaton information --");
		System.out.println("Number of states: " + states.length);
		
		int transitions = 0;
		for(Entry<State, Map<Integer, State>> e: mu.entrySet()) {
			transitions += e.getValue().entrySet().size();
		}
		
		System.out.println("Number of transitions: " + transitions);
		System.out.println("Number of values defined for rho: " + rho.entrySet().size());
		System.out.println("---------------------------");
	}
}
