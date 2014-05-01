package automata.ldfts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
			int symbol = w.get(0);
			
			Map<Integer, List<State>> transitions = a.getTransitions(state);
			
		}
		
		
		
		
		return adjacentSearchStates;
	}
}
