package algorithms.bfgs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import algorithms.Membership;
import automata.RegisterAutomaton;
import automata.State;

/**
 * <p>A search state used for characterization of the following elements:
 * <ul>
 * <li>which state of the automaton we are in</li>
 * <li>what the current value of the registers is</li>
 * <li>what the most recently used symbol was to get to this state</li>
 * </ul></p>
 * 
 * <p>Notice that, as opposed to nondeterministic membership decision,
 * this search state does not have a word "to analyse next". The most recent
 * symbol will be used later to retrace what kind of word path was taken.</p>
 * 
 * <p>For the moment this is generally not automaton-polymorphic, but we're working on that.</p>
 * 
 * <p>The core of this class is the definition of the hash and equality check functions,
 * which are necessary for the Graph-search component of the proposed algorithm.</p>
 * @author vincent
 *
 */
public class SearchState {
	public final State state;
	public final int[] registers;
	public final RegisterAutomaton a;
	public final int trSymbol;
	
	public SearchState(State state, int[] registers, RegisterAutomaton a, int trSymbol) {
		this.state = state;
		this.registers = registers;
		this.a = a;
		this.trSymbol = trSymbol;
	}
	
	public boolean isFinal() {
		return state.isFinal;
	}
	
	public List<SearchState> expand() {
		List<SearchState> adjacentSearchStates = new ArrayList<>();
		
		//Get the declared transitions
		Map<Integer, List<State>> transitions = a.getTransitions(state);
		
		/*
		 * All of these transitions are made up of a label and a list of accessible 
		 * states from that label. Each label identifies a register (because it's its 
		 * index) and:
		 * - if that register is full, we will append to the current word the symbol
		 *   that is currently in that register;
		 * - if that register is empty, we'll have a look at rho. If rho happens to 
		 *   point to that register, that's great, we append "a new letter"; else we
		 *   abandon the considered label.
		 */
		Integer rho = a.getAssignmentRegister(state);
		
		for(Entry<Integer, List<State>> labelTr : transitions.entrySet()) {
			//take a look at the register
			if(registers[labelTr.getKey()] >= 0) {
				//If assigned, take the same symbol, and generate the new 
				//searchstates with it
				int symbol = registers[labelTr.getKey()];
				for(State nextState : labelTr.getValue()) {
					adjacentSearchStates.add(new SearchState(nextState, registers.clone(), a, symbol));
				}
			} else if(rho != null && rho == labelTr.getKey()) {
				//Take a new value
				int symbol = chooseNewSymbol();
				registers[labelTr.getKey()] = symbol;
				for(State nextState : labelTr.getValue()) {
					adjacentSearchStates.add(new SearchState(nextState, registers.clone(), a, symbol));
				}
			}
		}
		
		return adjacentSearchStates;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(registers);
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchState other = (SearchState) obj;
		if (!Arrays.equals(registers, other.registers))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}
	
	private int chooseNewSymbol() {
		HashSet<Integer> registerContents = new HashSet<>();
		for(int i : registers) {
			registerContents.add(i);
		}
		
		for(int s = 1; ;s++) {
			if(!registerContents.contains(s))
				return s;
		}
	}
	
	
}
