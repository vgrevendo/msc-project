package algorithms.membership.bflgs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import algorithms.Tools;
import automata.RegisterAutomaton;
import automata.State;

/**
 * <strong style="color: red;">This class is BFLGS-specific.</strong>
 * 
 * <p>
 * A search state used for characterization of the following elements:
 * <ul>
 * <li> which state of the automaton we are in</li>
 * <li> what the current value of the registers is</li>
 * <li> what is left of the word we have to analyse</li>
 * </ul>
 * </p>
 * <p>For the moment this is generally not automaton-polymorphic, but we're working on that.</p>
 * @author vincent
 *
 */
public class BFLGSSearchState {
	public final State state;
	public final int[] registers;
	public final List<Integer> w;
	public final RegisterAutomaton a;
	
	public BFLGSSearchState(State state, int[] registers, List<Integer> word, RegisterAutomaton a) {
		this.state = state;
		this.registers = registers;
		this.w = word;
		this.a = a;
	}
	
	public boolean isFinal() {
		return w.size() == 0 && state.isFinal;
	}
	
	public List<BFLGSSearchState> expand() {
		if(w.size() <= 0)
			return new ArrayList<>();
			
		//If search state is not terminal, find the adjacent search states
		List<BFLGSSearchState> adjacentSearchStates = new ArrayList<>();
		
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
				adjacentSearchStates.add(new BFLGSSearchState(s, registers.clone(), w.subList(1, w.size()), a));
			}
		
		return adjacentSearchStates;
	}

	@Override
	/**
	 * This hashcode function has been minimised for BFLGS:
	 * the word-to-explore is ignored because BFLGS makes sure 
	 * it's the same everywhere in the frontier.
	 */
	public int hashCode() {
		return 31 * Arrays.hashCode(registers) + state.hashCode();
	}
	
	/**
	 * This function has therefore been minimised as well
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BFLGSSearchState other = (BFLGSSearchState) obj;
		if (!Arrays.equals(registers, other.registers))
			return false;
		if (state != other.state)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return state.name + " " + Arrays.toString(registers)
				+ ", w:" + w.subList(0, Math.min(4, w.size())).toString(); 
	}
	
}
