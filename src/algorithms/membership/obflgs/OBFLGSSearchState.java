package algorithms.membership.obflgs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import algorithms.Tools;
import automata.OptimisedRA;
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
public class OBFLGSSearchState {
	public final State state;
	public final List<Integer> fullWord;
	public final int nextIdx;
	public final OptimisedRA a;
	
	public final int[] registers;
	
	public OBFLGSSearchState(State state, 
							int[] registers, 
							List<Integer> fullWord,
							int nextIdx,
							OptimisedRA a) {
		this.state = state;
		this.registers = registers;
		this.nextIdx = nextIdx;
		this.a = a;
		this.fullWord = fullWord;
	}
	
	public boolean isFinal() {
		return state.isFinal;
	}
	
	public List<OBFLGSSearchState> expand() {
		if(nextIdx >= fullWord.size())
			return new ArrayList<>();
			
		//If search state is not terminal, find the adjacent search states
		List<OBFLGSSearchState> adjacentSearchStates = new ArrayList<>();
		
		//Get the next symbol
		int symbol = fullWord.get(nextIdx);
		
		//Update the registers and find the containing register (default -1)
		int containingRegister = -1;
		Integer assignmentRegister = -1;
		
		if((containingRegister = a.findContainingRegister(registers, symbol)) < 0) {
			//If a rho value is defined
			if((assignmentRegister = a.getAssignmentRegister(state)) != null) {
				containingRegister = assignmentRegister + a.getWriteableOffset();
				registers[assignmentRegister] = symbol;
			}
		}
		
		//Deduce possible transitions
		List<State> adjacentStates = a.getNextStates(state, containingRegister);
		
		//Infer search states
		if(adjacentStates != null)
			for(State s: adjacentStates) {
				adjacentSearchStates.add(new OBFLGSSearchState(s, registers.clone(), fullWord, nextIdx+1, a));
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
		OBFLGSSearchState other = (OBFLGSSearchState) obj;
		if (!Arrays.equals(registers, other.registers))
			return false;
		if (state != other.state)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return state.name + " " + Arrays.toString(registers)
				+ ", w:" + fullWord.subList(nextIdx, Math.min(nextIdx+4, fullWord.size())).toString(); 
	}
	
}
