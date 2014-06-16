package automata.greedy;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Set;

import automata.Automaton;

/**
 * <p>An automaton that will be used to act "greedily" when
 * applying the search algorithm: only part of the frontier 
 * will be expanded.</p>
 * <p>The difference is that we need much more preprocessing
 * (analyse states and transitions) and much more intelligent
 * sub objects (greedy states).</p>
 * <p>Instead of extending the usual set of classes, we will 
 * choose a completely different approach; for instance, all
 * components are contained within states, rather than 
 * externally.</p>
 * @author vincent
 *
 */
public class GreedyRA extends Automaton {
	private GreedyState[] states;
	private GreedyState initialState;
	private int[] registers;
	private int[] fixedRegisters;
	private int writeOffset;

	public GreedyRA(String loadPath) throws FileNotFoundException,
			ParseException {
		loadFromFile(loadPath);
		postOptimise();
	}

	//Reading from file
	private void loadFromFile(String path) throws FileNotFoundException, ParseException {
		//Check file correctness
		Scanner sc = new Scanner(new File(path));
		
		//Load components
		int lineNumber = 0;
		lineNumber = loadStates(sc);
		lineNumber = loadRegisters(sc, lineNumber);
		loadTransitions(sc, lineNumber);
	}
	private int loadStates(Scanner sc) throws ParseException {
		int lineNumber = 0;
		ArrayList<GreedyState> stateSet = new ArrayList<>();
		
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
					states = new GreedyState[stateSet.size()];
					stateSet.toArray(states);
					
					return lineNumber;
				} else {
					stateSet.add(new GreedyState(tokens[0], false, -1));
				}
				break;
			case 2:
				stateSet.add(new GreedyState(tokens[0], false, 
											 Integer.parseInt(tokens[1])-1));
				break;
			case 3:
				//Analyse rho
				int rho = -1;
				if(!tokens[1].equals("_"))
					rho = Integer.parseInt(tokens[1])-1;
				
				//Create state
				stateSet.add(new GreedyState(tokens[0], tokens[2].equals("F"), rho));
				break;
			default:
				throw new ParseException("Unrecognised line", lineNumber);
			}
		}
		
		throw new ParseException("File incomplete", lineNumber);
		
	}
	private int loadRegisters(Scanner sc, int lineNumber) throws ParseException {
		if(!sc.hasNext()) 
			throw new ParseException("Expected declaration of initial state", lineNumber);
		
		String token = sc.nextLine();
		
		for(GreedyState state : states) {
			if(state.name.equals(token)) {
				initialState = state;
				break;
			}
		}
		
		if(!sc.hasNext()) 
			throw new ParseException("Incomplete source file: no registers", lineNumber);
			
		String[] tokens = sc.nextLine().split(" ");
		registers = new int[tokens.length];
		
		for(int i = 0; i < tokens.length; i++) {
			registers[i] = tokens[i].equals("#") ? -1 : Integer.parseInt(tokens[i]);
		}
		
		sc.nextLine();
		return lineNumber+2;
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
				throw new ParseException("Expected 'source label destination'", lineNumber);
			
			//Lookup states: could be optimised with a map kept from before
			GreedyState state1 = null;
			GreedyState state2 = null;
			
			for(GreedyState state : states) {
				if(state.name.equals(tokens[0])) {
					state1 = state;
					break;
				}
			}
			for(GreedyState state : states) {
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
			
			// Insert into state data
			for(int label : labels)
				state1.addTransition(label, state2);
		}
		
		return lineNumber;
	}

	//Greedy behaviour and optimisations related
	private void postOptimise() {
		detectFixedRegisters();
		computeStateCharacteristics();
	}
	private void detectFixedRegisters() {
		//Get a set full of read-only register indexes
		Set<Integer> writableRegisters = new HashSet<>();
		Set<Integer> readOnlyRegisters = new HashSet<>();
		
		for(int i = 0; i < registers.length; i++) {
			readOnlyRegisters.add(i);
		}
		
		for(GreedyState state : states) {
			int rho = state.getAssignmentRegister();
			if(rho >= 0) {
				readOnlyRegisters.remove(rho);
				writableRegisters.add(rho);
			}
		}
		
		//Rewrite registers
		// Rewritemap is a map that maps old indexes to new ones
		Map<Integer, Integer> rewriteMap = new HashMap<Integer, Integer>();
		int[] oldRegisters = registers;
		registers = new int[writableRegisters.size()];
		fixedRegisters = new int[readOnlyRegisters.size()+1];
		writeOffset = readOnlyRegisters.size();
		
		int counter = 0;
		for(Integer i : readOnlyRegisters) {
			rewriteMap.put(i, counter);
			fixedRegisters[oldRegisters[i]] = counter;
			counter++;
		}
		for(Integer i : writableRegisters) {
			rewriteMap.put(i, counter);
			registers[counter-fixedRegisters.length+1] = oldRegisters[i];
			counter++;
		}
		
		//Rewrite rho values
		for(GreedyState state : states) {
			//To be done differently and cleanly!
			if(state.getAssignmentRegister() >= 0)
				state.setRho(rewriteMap.get(state.getAssignmentRegister())-writeOffset);
		}
		
		//Rewrite transitions: TODO more cleanly
		for(GreedyState state : states) {
			Map<Integer, List<GreedyState>> newTransitions = new HashMap<>();
			for(Entry<Integer, List<GreedyState>> e : state.getTransitions().entrySet()) {
				newTransitions.put(rewriteMap.get(e.getKey()), new ArrayList<GreedyState>());
				for(GreedyState s : e.getValue())
					newTransitions.get(rewriteMap.get(e.getKey())).add(s);
			}
			
			state.clearMu(); //Stupid!
			
			//Rewrite all transition labels for all destination states
			for(Entry<Integer, List<GreedyState>> e : newTransitions.entrySet())
				for(GreedyState s : e.getValue())
					state.addTransition(e.getKey(), s);
		}
		
		//And we're ready to go.
	}
	/**
	 * <p>Compute some useful characteristics by calling the specialised
	 * method in the target state.</p>
	 * <p>Stable states are states from which any register is labelled
	 * in at least one transition, and have
	 * a rho value. If any of these conditions is not verified, 
	 * the state is unstable.</p>
	 * <p>Stable states are both a good thing and a bad thing: most of the
	 * time a configuration containing them doesn't need to be expanded, 
	 * but on the other hand they do represent a lot of programming work.</p>
	 */
	private void computeStateCharacteristics() {
		for(GreedyState state : states) 
			state.computeCharacteristics(registers.length + writeOffset, writeOffset);
	}
	
	//Tools
	public void displayInfo() {
		System.out.println("-- Automaton information --");
		System.out.println("Number of states: " + states.length);
		
		int transitions = 0;
		for(GreedyState state : states)
			for(Entry<Integer, List<GreedyState>> e: state.getTransitions().entrySet())
				transitions += e.getValue().size();
		System.out.println("Number of registers: " + registers.length);
		System.out.println("Number of transitions: " + transitions);
		
		int rhos = 0;
		for(GreedyState state : states)
			if(state.getAssignmentRegister() >= 0)
				rhos ++;
		System.out.println("Number of values defined for rho: " + rhos);
		
		int fStates = 0;
		for(GreedyState s : states) {
			if(s.isFinal)
				fStates ++;
		}
		System.out.println("Number of final states: " + fStates);
		
		int stables = 0;
		for(GreedyState s : states) {
			stables += s.isStable() ? 1 : 0;
		}
		System.out.println("Number of stable states: " + stables);
		
		System.out.println("---------------------------");
	}
	private boolean lineIsComment(String line) {
		return line.length() >= 2 && line.substring(0, 2).equalsIgnoreCase("--");
	}
	
	//Access methods
	public GreedyState getInitialState() {
		return initialState;
	}
	public GreedyState[] getStates() {
		return states;
	}
	public int[] getInitialRegisters() {
		return registers.clone();
	}
	public int[] getFixedRegisters() {
		return fixedRegisters;
	}
	public int findContainingRegister(int[] registers, int symbol) {
		if(symbol > 0 && symbol < fixedRegisters.length) 
			return fixedRegisters[symbol];
		int i =0;
		for(int s : registers) {
			if(s == symbol)
				return writeOffset + i;
			i++;
		}
		return -1;
	}
	public int getWriteableOffset() {
		return writeOffset;
	}
	public int getReadOnlyRegister(int index) {
		return fixedRegisters[index];
	}
}
