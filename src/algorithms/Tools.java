package algorithms;

import java.util.List;
import java.util.Map.Entry;

import automata.RegisterAutomaton;
import automata.State;

/**
 * A static-method populated class representing a set of tools
 * @author vincent
 *
 */
public class Tools {
	public static boolean isDeterministic(RegisterAutomaton a) {
		State[] states = a.getStates();
		
		for(State s : states) {
			for(Entry<Integer, List<State>> e : a.getTransitions(s).entrySet()) {
				if(e.getValue().size() > 1) {
					return false;
				} 
			}
		}
		
		return true;
	}
}
