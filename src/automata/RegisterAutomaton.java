package automata;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import algorithms.Tools;

/**
 * A register automaton as defined in "Finite-Memory Automata", Kaminski, Francez
 * @author vincent
 *
 */
public class RegisterAutomaton extends Automaton {
	
	protected State[] states;

	protected State initialState;
	protected int[] registers;
	/**
	 * Instead of a function, rho is a relation as well.
	 * This avoids creating function objects.
	 */
	protected Map<State, Integer> rho;
	/**
	 * Mu is the relation as defined in the paper.
	 * Nullpointers will have to be handled intelligently here.
	 */
	protected Map<State, Map<Integer, List<State>>> mu;
	
	public RegisterAutomaton(String loadPath) throws FileNotFoundException, ParseException {
		loadFromFile(loadPath);
	}
	
	/**
	 * This constructor is useful if you want to extend this class
	 * without copying the values of an automaton
	 * @param ra
	 */
	public RegisterAutomaton(RegisterAutomaton ra) {
		states = ra.states;
		initialState = ra.initialState;
		registers = ra.registers;
		
		rho = ra.rho;
		mu = ra.mu;
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
			String line = sc.nextLine();
			
			if(lineIsComment(line))
				continue;
				
			String[] tokens = line.split(" ");
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
						mu.put(state, new HashMap<Integer, List<State>>());
					}
					
					return lineNumber;
				} else {
					State state = new State(tokens[0]);
					stateSet.add(state);
				}
				break;
			case 2:
				State state = new State(tokens[0]);
				stateSet.add(state);
				rho.put(state, Integer.parseInt(tokens[1])-1);
				break;
			case 3:
				State finalState = new State(tokens[0], tokens[2].equals("F"));
				stateSet.add(finalState);
				if(!tokens[1].equals("_"))
					rho.put(finalState, Integer.parseInt(tokens[1])-1);
				break;
			default:
				throw new ParseException("Unrecognised line", lineNumber);
			}
		}
		
		throw new ParseException("File incomplete", lineNumber);
		
	}

	/**
	 * Load initial state and registers
	 * @param sc
	 * @param lineNumber
	 * @return
	 * @throws ParseException
	 */
	private int loadRegisters(Scanner sc, int lineNumber) throws ParseException {
		if(sc.hasNext()) {
			String token = sc.nextLine();
			
			for(State state : states) {
				if(state.name.equals(token)) {
					initialState = state;
					break;
				}
			}
		} else {
			throw new ParseException("Expected declaration of initial state", lineNumber);
		}
		
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
			String line = sc.nextLine();

			if(lineIsComment(line))
				continue;
			
			String[] tokens = line.split(" ");
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
			
			//Handle multi-labels
			// Parse
			ArrayList<Integer> labels = new ArrayList<>();
			if(tokens[1].equals("*")) {
				for(int l = 0; l < registers.length; l++)
					labels.add(l);
			} else {
				String[] labelTokens = tokens[1].split(",");
				for(String labelToken : labelTokens)
					labels.add(Integer.parseInt(labelToken)-1);
			}
			
			// Insert
			for(int label : labels) {
				if(!mu.get(state1).containsKey(label))
					mu.get(state1).put(label, new ArrayList<State>());
				mu.get(state1).get(label).add(state2);
			}
		}
		
		return lineNumber;
	}

	public void displayInfo() {
		System.out.println("-- Automaton information --");
		System.out.println("Number of states: " + states.length);
		
		int transitions = 0;
		for(Entry<State, Map<Integer, List<State>>> e: mu.entrySet()) {
			for(Entry<Integer, List<State>> e2: e.getValue().entrySet()) {
				transitions += e2.getValue().size();
			}
		}
		
		System.out.println("Number of registers: " + registers.length);
		
		System.out.println("Number of transitions: " + transitions);
		System.out.println("Number of values defined for rho: " + rho.entrySet().size());
		
		int fStates = 0;
		for(State s : states) {
			if(s.isFinal)
				fStates ++;
		}
		
		System.out.println("Number of final states: " + fStates);
		
		if(Tools.isDeterministic(this))
			System.out.println("This automaton is deterministic");
		else
			System.out.println("This automaton is nondeterministic");
		
		System.out.println("---------------------------");
	}

	//Access methods
	public State getInitialState() {
		return initialState;
	}
	public State[] getStates() {
		return states;
	}
	public Map<Integer, List<State>> getTransitions(State s) {
		return mu.get(s);
	}


	
	/**
	 * Registers of an automaton model are immutable!
	 * @return
	 */
	public int[] getInitialRegisters() {
		return registers.clone();
	}
	
	public Integer getAssignmentRegister(State s) {
		return rho.get(s);
	}
	
	public List<State> getNextStates(State currentState, int label) {
		return mu.get(currentState).get(label);
	}

	//Tools
	private boolean lineIsComment(String line) {
		return line.length() >= 2 && line.substring(0, 2).equalsIgnoreCase("--");
	}
}
