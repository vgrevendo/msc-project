package algorithms.emptiness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	public final int[] fullAssignment;
	
	public SearchState(State state, int[] registers, 
					   RegisterAutomaton a, int[] fullAssignment) {
		this.state = state;
		this.registers = registers;
		this.a = a;
		this.fullAssignment = fullAssignment;
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
		 * - if that register is empty, we ignore it.
		 * - we check if rho points to an empty register, and if it does that register 
		 *   becomes eligible as well.
		 */
		Integer irho = a.getAssignmentRegister(state);
		int rho = irho != null ? irho : -1;
		
		for(Entry<Integer, List<State>> labelTr : transitions.entrySet()) {
			int regIndex = labelTr.getKey();
			
			//take a look at the register
			if(registers[regIndex] >= 0) {
				//If assigned, take the same symbol, and generate the new 
				//searchstates with it
				for(State nextState : labelTr.getValue()) {
					adjacentSearchStates.add(new SearchState(nextState, registers.clone(), a, fullAssignment));
				}
			}
		}
		
		//If rho is set and the assigned register is not created
		if(rho >= 0 && registers[rho] < 0) {
			//Pick the symbol from the full assignment
			registers[rho] = fullAssignment[rho];
			for(State nextState : transitions.get(irho)) {
				adjacentSearchStates.add(new SearchState(nextState, registers.clone(), a, fullAssignment));
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

	@Override
	public String toString() {
		return "[" + state + ": " + Arrays.toString(registers) + "]";
	}
}
