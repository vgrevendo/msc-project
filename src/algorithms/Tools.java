package algorithms;

import java.util.HashSet;
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

	/**
	 * As explained in the PROPOSITION, it is only necessary to work with a small subset 
	 * of the infinite alphabet. This method computes a list representing that subset.
	 * @param a
	 * @return
	 */
	public static Integer[] computeMinimalAlphabet(RegisterAutomaton a) {
		//Determine alphabet subset
		int[] ir = a.getInitialRegisters();
		HashSet<Integer> subset = new HashSet<>();
		
		int max = 0;
		
		for(int i : ir) {
			if(i >= 0) {
				subset.add(i);
				if(i > max)
					max = i+1;
			}
		}
		
		//See explanations in document
		int toAdd = ir.length - subset.size() + 1;
		
		for(int j = max; j < max + toAdd; j++)
			subset.add(j);
		
		Integer[] sequence = new Integer[subset.size()];
		subset.toArray(sequence);
		
		return sequence;
	}
}
