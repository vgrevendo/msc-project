package automata.gen;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import automata.State;

/**
 * <p>Generate an automaton from a given specification file (SFA).
 * This class respects the first definition of the specification
 * of that language.</p>
 * 
 * <p>Building will not generate errors, only warnings</p>
 * @author vincent
 *
 */
public class SpecificationSynthGenerator extends AutomatonGenerator {
	private Scanner sc;
	
	//Building resources
	private Map<String, State> states;
	private State initialState;
	private Map<State, Map<State, ArrayList<String[]>>> transitions;
	private int intermediateStateCounter = 0;
	private int numRegisters = 0;
	private int rho = -1;

	public SpecificationSynthGenerator(String pathToSource) throws FileNotFoundException {
		super("SPEC-GEN");
		
		this.sc = new Scanner(new File(pathToSource));
	}

	@Override
	protected void build() throws BuildException {
		//Read input file
		extractStates();
		extractRegisters();
		extractTransitions();
		
		//Build output file
		process();
	}
	
	private void extractStates() throws BuildException {
		//Extract the names from the source file
		states = new HashMap<String, State>();
		
		while(sc.hasNext()) {
			String line = sc.nextLine();
			
			if(lineIsComment(line))
				continue;
			if((line = line.trim()).equals("-"))
				break;
			
			String[] tokens = line.trim().split(" ");
			boolean f = false;
			
			switch(tokens.length) {
			case 2:
				if(!tokens[1].equals("F")) 
					throw new BuildException("Unrecognised token: " + tokens[1]);
				f = true;
			case 1:
				states.put(tokens[0], new State(tokens[0], f));
				break;
			default:
				throw new BuildException("Expected state description: NAME [F]");
			}
		}
		
		if(states.size() == 0)
			throw new BuildException("No states found!");
	}
	
	private void extractRegisters() throws BuildException {
		if(!sc.hasNext())
			throw new BuildException("No initial state specified!");
		String line = sc.nextLine().trim();
		
		//Extract initial state
		initialState = states.get(line);
		
		if(initialState == null)
			throw new BuildException("Initial state does not refer to any known state!");
		
		//Extract registers
		if(!sc.hasNext())
			throw new BuildException("Expected registers description!");
		line = sc.nextLine().trim();
		
		for(String reg : line.split(" ")) {
			addRegister(reg.equals("#") ? -1 : Integer.parseInt(reg));
			numRegisters++;
		}
		
		if(!sc.hasNext())
			throw new BuildException("Incomplete source file: no transitions description!");
		sc.nextLine();
	}
	
	private void extractTransitions() throws BuildException {
		transitions = new HashMap<>();
		
		//Add all states
		for(Entry<String, State> e : states.entrySet()) {
			transitions.put(e.getValue(), new HashMap<State, ArrayList<String[]>>());
		}
		
		//Then handle transitions
		while(sc.hasNext()) {
			String line = sc.nextLine().trim();
			
			if(lineIsComment(line))
				continue;
			
			String[] tokens = line.split(" ");
			if(tokens.length != 5)
				throw new BuildException("Unexpect transition format: "+ line);
			
			State start = states.get(tokens[0]);
			State end = states.get(tokens[4]);
			if(start == null || end == null)
				throw new BuildException("Unreferenced transition states: " + start.name + " or " + end.name);
			
			if(!transitions.get(start).containsKey(end))
				transitions.get(start).put(end, new ArrayList<String[]>());
			transitions.get(start).get(end).add(new String[] {tokens[1], tokens[2], tokens[3]});
		}
	}
	
	private void process() throws BuildException {
		for(Entry<State, Map<State, ArrayList<String[]>>> startEntry : transitions.entrySet()) {
			State start = startEntry.getKey();
			
			//For this start state, examine all transitions, and determine
			// which rho value to output
			rho = -1;
			
			for(Entry<State, ArrayList<String[]>> endEntry : transitions.get(start).entrySet()) {
				State end = endEntry.getKey();
				
				//Now we have a start and end states pair.
				// Examine each transition
				
				for(String[] transition : transitions.get(start).get(end)) {
					//Construct each transition and add states
					String s1 = constructTransition(null, transition[2], end.name);
					String s2 = constructTransition(null, transition[1], s1);
					constructTransition(start.name, transition[0], s2);	
				}
			}
			
			//Build start state
			addState(start.name, rho, start.isFinal);
		}
	}
	
	//Tools
	private boolean lineIsComment(String line) {
		return line.length() >= 2 && line.substring(0, 2).equalsIgnoreCase("--");
	}
	
	private String addIntermediateState(int rho) {
		String s = "int" + intermediateStateCounter;
		intermediateStateCounter++;
		addState(s, rho, false);
		return s;
	}
	
	/**
	 * Add the transition between two state names, by parsing the transition.
	 * If the start state does not exist, its name will be deduced from the
	 * transition parsing.
	 * @param start
	 * @param t
	 * @param end
	 * @return the rho value for the start state
	 * @throws BuildException 
	 */
	private String constructTransition(String start, String t, String end) throws BuildException {
		//Start by parsing the transition string
		ArrayList<Integer> labels = new ArrayList<>();
		int rho = -1;
		
		if(t.contains("!")) {
			if(t.equals("*!")) {
				for(int i = 0; i < numRegisters; i++)
					labels.add(i);
				rho = numRegisters-1;
			} else {
				String labelString = t.substring(0, t.length()-1);
				rho = Integer.parseInt(labelString)-1;
				labels.add(rho);
			}
		} else if(t.equals("*")) {
			for(int i = 0; i < numRegisters; i++)
				labels.add(i);
		} else {
			String[] tokens = t.split(",");
			for(String token : tokens) {
				labels.add(Integer.parseInt(token)-1);
			}
		}
		
		//If the start state is not there, build it
		if(start == null)
			start = addIntermediateState(rho);
		else if(rho >= 0){
			if(this.rho >= 0)
				throw new BuildException("Conflicting rho values for state " + start);
			else
				this.rho = rho;
		}
		
		//Construct the transitions between both states
		for(Integer l : labels) {
			addTransition(start, end, l);
		}
		
		return start;
	}
}
