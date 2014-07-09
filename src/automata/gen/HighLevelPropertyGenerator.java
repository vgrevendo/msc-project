package automata.gen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

public class HighLevelPropertyGenerator extends AutomatonGenerator {
	private final Scanner sc;
	private final String inputFileName;
	
	//Stuff to remember
	private final HashMap<State, HashMap<Integer, Set<State>>> transitions;
	private final HashMap<String, Integer> variablesRegisters;
	private final HashMap<String, Integer> shortcutRegisters = new HashMap<>();
	private final HashMap<String, String> observers;
	private final Set<String> observerNames = new HashSet<>();
	private final Set<String> observerRoots = new HashSet<>();
	private final HashMap<String, State> statesByName = new HashMap<>();
	private final Set<String> assignmentShortcuts = new HashSet<>();
	
	private Integer[] registers;
	private int trashRegister;
	private int intermediaryStateCounter = 0;
	
	//Flags
	private boolean implicitSet = false;
	private boolean variablesSet = false;

	public HighLevelPropertyGenerator(String filename) throws FileNotFoundException {
		super("HLP-GEN");
		
		sc = new Scanner(new File(filename));
		inputFileName = filename;
		transitions = new HashMap<>();
		variablesRegisters = new HashMap<>();
		observers = new HashMap<>();
	}

	@Override
	protected void build() throws BuildException {
		//Parse file
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			
			if(!line.substring(0, 2).equals("--"))
				evaluate(line);
		}
		
		//Complete states to make them stable
		completeStates();
		
		//Commit parsing properties
		buildAutomaton();
		
		//Build TRF file
		buildTRFFile();
	}

	private void buildTRFFile() throws BuildException {
		Random r = new Random();
		
		//Pick filename
		String basename = inputFileName.split("/")[1].split("\\.")[0];
		String trfFilename = "gen/" + basename + r.nextInt() + ".trf";
		
		//Write to that file
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(trfFilename);
		} catch (FileNotFoundException e) {
			throw new BuildException("Could not init TRF file: " + e.getMessage());
		}
		
		for(Entry<String, String> e : observers.entrySet()) {
			String methodPath = e.getKey();
			int lastDotIndex = methodPath.lastIndexOf(".");
			String clRoot = methodPath.substring(0, lastDotIndex);
			String method = methodPath.substring(lastDotIndex+1);
			Integer methodShortcutCode = shortcutRegisters.get(e.getValue());
			
			pw.print("subclass:");
			pw.print(clRoot);
			pw.print(" ");
			pw.print(method);
			pw.print(":");
			pw.print(methodShortcutCode);
			pw.print(" ");
			
			if(assignmentShortcuts.contains(e.getValue()))
				pw.println("object:ID");
			else
				pw.println("_");
		}
		
		if(pw != null)
			pw.close();
	}

	/**
	 * Submit the properties to superclass
	 */
	private void buildAutomaton() {
		//Build states (only error is final)
		for(Entry<String, State> e : statesByName.entrySet()) {
			State s = e.getValue();
			addState(s.name, s.rho, s.name.equals("error"));
		}
		
		//Build initial state
		setInitialState("start");
		
		//Build registers
		for(int r = 0; r < registers.length; r++)
			addRegister(registers[r]);
		
		//Build transitions
		for(Entry<State, HashMap<Integer, Set<State>>> e : transitions.entrySet()) {
			State source = e.getKey();
			for(Entry<Integer, Set<State>> f: e.getValue().entrySet()) {
				Integer label = f.getKey();
				for(State destination : f.getValue()) {
					addTransition(source.name, destination.name, label);
				}
			}
		}
	}

	/**
	 * All non-intermediary states should be stable!
	 */
	private void completeStates() {
		for(Entry<String, State> e: statesByName.entrySet()) {
			State s = e.getValue();
			if(s.intermediate)
				continue;
			
			if(!transitions.containsKey(s))
				transitions.put(s, new HashMap<Integer, Set<State>>());
			
			for(int r = 0; r < registers.length; r++) {
				if(!transitions.get(s).containsKey(Integer.valueOf(r))) {
					buildTransition(s, s, r);
				}
			}
		}
	}

	private void evaluate(String line) throws BuildException {
		String[] tokens = line.split(" ");
		
		switch(tokens[0]) {
		case "observe":
			addObserver(tokens);
			break;
		case "implicit":
			addImplicitObservers(tokens);
			break;
		case "variables":
			addVariables(tokens);
			break;
		default:
			addTransition(tokens);
			break;
		}
	}
	
	private void addObserver(String methodPath, String shortcut, String clRoot) throws BuildException {
		if(implicitSet)
			throw new BuildException("Got observer command for " + methodPath + " after implicit!");
		if(variablesSet)
			throw new BuildException("Got observer command for " + methodPath + " after variables!");
		
		observers.put(methodPath, shortcut);
		observerNames.add(shortcut);
		observerRoots.add(clRoot);
	}
	
	private void addObserver(String[] tokens) throws BuildException {
		String clRoot = tokens[2].substring(0, tokens[2].lastIndexOf("."));
		addObserver(tokens[2], tokens[1], clRoot);
	}
	
	/**
	 * Add an observer for all roots, for the method specified
	 * @param tokens
	 * @throws BuildException
	 */
	private void addImplicitObservers(String[] tokens) throws BuildException {
		String methodName = tokens[1];
		
		for(String root : observerRoots) {
			String methodPath = root + "." + methodName;
			addObserver(methodPath, methodPath, root);
		}
		implicitSet = true;
	}
	
	/**
	 * Init the registers if needed, and bind the variables
	 * @param tokens
	 * @throws BuildException 
	 */
	private void addVariables(String[] tokens) throws BuildException {
		//Init registers if necessary
		if(!variablesSet) {
			variablesSet = true;
			int numVariables = tokens.length - 1;
			
			//Count method shortcuts
			Set<String> shortcutSet = new HashSet<String>(observers.values());
			int numShortcuts = shortcutSet.size();
			
			//Init registers
			registers = new Integer[numShortcuts + numVariables + 1]; //+1 for trash
			int r = 0;
			for(String shortcut : shortcutSet) {
				registers[r] = r;
				shortcutRegisters.put(shortcut, r);
				r++;
			}
			for(int v = 0; v < numVariables; v++) {
				registers[r] = -1;
				variablesRegisters.put(tokens[v+1], r);
				r++;
			}
			registers[r] = -1;//trash
			trashRegister = r;
		} else
			throw new BuildException("Already had variables!");
	}
	
	private void addTransition(String[] tokens) throws BuildException {
		//Parse line
		if(tokens.length < 5)
			throw new BuildException("Incomplete line: " + tokens.toString());
		
		String state1 = tokens[0];
		if(!tokens[1].equals("->"))
			throw new BuildException("Unidentified state separator: " + tokens[1]);
		String state2 = tokens[2];
		if(!tokens[3].equals(":"))
			throw new BuildException("Unidentified transition separator: " + tokens[3]);
		
		//Build missing states
		if(!statesByName.containsKey(state1))
			statesByName.put(state1, new State(state1, false));
		if(!statesByName.containsKey(state2))
			statesByName.put(state2, new State(state2, false));
		
		State s1 = statesByName.get(state1);
		State s2 = statesByName.get(state2);
		
		//Translate transitions
		//Special case transitions
		//The "*" macro
		if(tokens.length == 5 && tokens[4].equals("*")) {
			if(!s1.equals(s2)) 
				throw new BuildException("Cannot use macro * if both states are not identical!");
			
			for(int r = 0; r < registers.length; r++)
				buildTransition(s1, s2, r);
			
			return;
		}
		//The init method
		if(tokens.length == 5 && tokens[4].contains("init")) {
			String[] methodTokens = tokens[4].split("\\.");
			System.out.println("Method tokens : " + Arrays.toString(methodTokens));
			handleInitMethod(s1, s2, methodTokens[0], methodTokens[1]);
			return;
		}
		//Default case
		for(int s = 4; s < tokens.length; s++) {
			String methodString = tokens[s];
			if(methodString.contains("init"))
				throw new BuildException("Improper use of init method!");
			if(methodString.equals("*"))
				throw new BuildException("Improper use of * macro!");
			
			String[] assignmentTokens = methodString.split("=");
			String[] callTokens = assignmentTokens[assignmentTokens.length-1].split("\\.");
			
			handleMethod(s1, s2, assignmentTokens.length > 1 ? assignmentTokens[0] : null, 
					     callTokens[0], callTokens[1]);
		}
	}
	
	private void handleMethod(State state1, State state2,
							  String assignmentVariable,
							  String targetVariable, String method) throws BuildException {
		Integer targetRegister = variablesRegisters.get(targetVariable);
		Integer methodRegister = shortcutRegisters.get(method);
		
		State firstLevel = null;
		
		//Merge first transition if needed
		if(transitions.containsKey(state1) && transitions.get(state1).containsKey(targetRegister)) {
			Set<State> destinationStates = transitions.get(state1).get(targetRegister);
			
			for(State s : destinationStates) {
				if(s.intermediate) {
					firstLevel = s;
					break;
				}
			}
		}
		//Make first destination state if needed
		if(firstLevel == null) {
			firstLevel = makeIntermediaryState();
			buildTransition(state1, firstLevel, targetRegister);
			buildTransition(firstLevel, state1, trashRegister);
		}
		
		//Make second destination state if needed
		if(assignmentVariable == null) { //Do not make since not assigning
			buildTransition(firstLevel, state2, methodRegister);
		} else { //Make, because assignment
			assignmentShortcuts.add(method);
			
			State secondLevel = makeIntermediaryState();
			int assignmentRegister = variablesRegisters.get(assignmentVariable);
			secondLevel.setRho(assignmentRegister);
			buildTransition(firstLevel, secondLevel, methodRegister);
			buildTransition(secondLevel, state2, assignmentRegister);
		}
	}
	
	private State makeIntermediaryState() {
		String name = "int" + intermediaryStateCounter;
		State state = new State(name, true);
		statesByName.put(name, state);
		
		intermediaryStateCounter++;
		
		return state;
	}

	/**
	 * Set rho for the source state and make direct transition between both states
	 * @param state1
	 * @param state2
	 * @param variable
	 * @throws BuildException 
	 */
	private void handleInitMethod(State state1, State state2, String variable, String method) throws BuildException {
		//Set rho
		Integer varRegister = variablesRegisters.get(variable);
		if(varRegister == null)
			throw new BuildException("Reference to undefined var " + variable);
		
		state1.setRho(varRegister);
		
		//Make transition
		Integer methodLabel = shortcutRegisters.get(method);
		if(methodLabel == null)
			throw new BuildException("Reference to undefined init method " + method);
		
		buildTransition(state1, state2, methodLabel);
	}
	
	private void buildTransition(State state1, State state2, Integer label) {
		if(!transitions.containsKey(state1))
			transitions.put(state1, new HashMap<Integer, Set<State>>());
		if(!transitions.get(state1).containsKey(label))
			transitions.get(state1).put(label, new HashSet<State>());
		transitions.get(state1).get(label).add(state2);
	}
	
	private class State {
		public final String name;
		public final boolean intermediate;
		private int rho;
		private boolean rhoSet = false;
		
		@Override
		public int hashCode() {
			return name.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			State other = (State) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			}
			return name.equals(other.name);
		}
		
		public State(String name, boolean intermediate) {
			this.name = name;
			this.rho = trashRegister;
			this.intermediate = intermediate;
		}
		
		public void setRho(int rho) throws BuildException {
			if(rhoSet)
				throw new BuildException("Conflicting values for rho in state " + name + " (" + rho + "," + this.rho + ")");
			
			rhoSet = true;
			this.rho = rho;
		}
	}
}
