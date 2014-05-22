package algorithms.membership;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import algorithms.Tools;
import automata.RegisterAutomaton;
import automata.State;

/**
 * A search state used for characterization of the following elements:
 * - which state of the automaton we are in
 * - what the current value of the registers is
 * - what is left of the word we have to analyse
 * 
 * For the moment this is generally not automaton-polymorphic, but we're working on that.
 * @author vincent
 *
 */
public class SearchState {
	public final State state;
	public final int[] registers;
	public final List<Integer> w;
	public final RegisterAutomaton a;
	
	public SearchState(State state, int[] registers, List<Integer> word, RegisterAutomaton a) {
		this.state = state;
		this.registers = registers;
		this.w = word;
		this.a = a;
	}
	
	public boolean isFinal() {
		return w.size() == 0 && state.isFinal;
	}
	
	public List<SearchState> expand() {
		List<SearchState> adjacentSearchStates = new ArrayList<>();
		
		//If search state is not terminal, find the adjacent search states
		if(w.size() > 0) {
			//Get the next symbol
			int symbol = w.get(0);
			
			//Update the registers and find the containing register (default -1)
			int containingRegister = -1;
			Integer assignmentRegister = -1;
			
			if((containingRegister = Tools.registersContain(registers, symbol)) < 0) {
				//If a rho value is defined
				if((assignmentRegister = a.getAssignmentRegister(state)) != null) {
					containingRegister = assignmentRegister;
					registers[containingRegister] = symbol;
				}
			}
			
			//Deduce possible transitions
			List<State> adjacentStates = a.getNextStates(state, containingRegister);
			
			//Infer search states
			if(adjacentStates != null)
				for(State s: adjacentStates) {
					adjacentSearchStates.add(new SearchState(s, registers.clone(), w.subList(1, w.size()), a));
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
		result = prime * result + ((w == null) ? 0 : w.hashCode());
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
		if (state != other.state)
			return false;
		if (w == null) {
			if (other.w != null)
				return false;
		} else if (!w.equals(other.w))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return state.name + " " + Arrays.toString(registers)
				+ ", w:" + w.subList(0, Math.min(4, w.size())).toString(); 
	}
	
}
