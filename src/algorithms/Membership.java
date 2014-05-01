package algorithms;

import java.util.List;

import automata.RegisterAutomaton;
import automata.State;

public class Membership {
	public static boolean isMember(RegisterAutomaton a, int[] w) {
		if(Tools.isDeterministic(a))
			return deterministicMemberCheck(a, w);
		
		return false;
	}
	
	/**
	 * A tiny method that needs a deterministic automaton in order to function (faster
	 * and easier to implement)
	 * @param a
	 * @param w
	 * @return
	 */
	private static boolean deterministicMemberCheck(RegisterAutomaton a, int[] w) {
		State currentState = a.getInitialState();
		int[] registers = a.getInitialRegisters();
		int containingRegister = 0;
		Integer assignmentRegister = 0;
		
		//Simply follow the path
		for(int wcursor = 0; wcursor < w.length; wcursor++) {
			//If our registers do not contain the current symbol
			if((containingRegister = registersContain(registers, w[wcursor])) < 0) {
				//If a rho value is defined
				if((assignmentRegister = a.getAssignmentRegister(currentState)) != null) {
					containingRegister = assignmentRegister;
					registers[containingRegister] = w[wcursor];
				}
			}
			
			//Make transition if possible
			List<State> nextStates = a.getNextStates(currentState, containingRegister);
			if(nextStates == null)
				//If we can't go any further
				return false;
			else {
				//Else make transition
				currentState = nextStates.get(0);
			}
		}
		
		return currentState.isFinal;
	}
	
	private static int registersContain(int[] registers, int symbol) {
		for(int i = 0; i < registers.length; i++) {
			if(registers[i] == symbol)
				return i;
		}
		return -1;
	}
	
	private static boolean nondeterministicMemberCheck(RegisterAutomaton a, int[] w) {
		return false;
	}
}
