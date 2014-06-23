package algorithms.membership.greedy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import testbench.Testbench;
import algorithms.tools.ResultsContainer;
import automata.greedy.GreedyRA;
import automata.greedy.GreedyState;

public class GreedyConfiguration {
	//Environment
	public final GreedyState state;
	public final GreedyRA a;
	public final int[] registers;
	
	//Status
	private boolean dead = false;
	private final int lastUpdateStep;
	
	//Caching
	private final int hc = 0; 
	
	//Statistics
	private static int nodesExpanded = 0;
	
	public GreedyConfiguration(GreedyState state, 
								int[] registers, 
								GreedyRA a,
								int lastUpdateStep) {
		this.state = state;
		this.registers = registers;
		this.a = a;
		this.lastUpdateStep = lastUpdateStep;
	}
	
	//Tools
	public List<GreedyConfiguration> expand(int symbol, int step, int previousSymbol) {
		//If search state is not terminal, find the adjacent search states
		List<GreedyConfiguration> adjacentSearchStates = new ArrayList<>();
		
		//Update the registers and find the containing register (default -1)
		Integer containingRegister = Integer.valueOf(-1);
		int assignmentRegister = -1;
		
		if((containingRegister = a.findContainingRegister(registers, symbol)) < 0) {
			//If a rho value is defined
			if((assignmentRegister = state.getAssignmentRegister()) >= 0) {
				containingRegister = Integer.valueOf(assignmentRegister + a.getWriteableOffset());
				registers[assignmentRegister] = symbol;
			}
		} else {
			//If we found a transition to make that does not include rho, we will
			// have to update the rho values that were skipped, if any.
			if(step > lastUpdateStep+1)
				registers[state.getAssignmentRegister()] = previousSymbol;
		}
		
		//Deduce possible transitions
		List<GreedyState> adjacentStates = state.getNextStates(containingRegister);
		
		//Infer configurations
		if(adjacentStates != null)
			for(GreedyState s: adjacentStates) 
				adjacentSearchStates.add(new GreedyConfiguration(s, registers.clone(), a, step));
		
		dead = true; //Do not expand this configuration again!
		if(Testbench.COLLECT_STATS)
			nodesExpanded++;
		return adjacentSearchStates;
	}

	//Generics
	@Override
	public int hashCode() {
		return hc;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GreedyConfiguration other = (GreedyConfiguration) obj;
		if(dead != other.dead)
			return false;
		if (!Arrays.equals(registers, other.registers))
			return false;
		if (state != other.state)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return state.name + ": " + Arrays.toString(registers); 
	}

	//Accessors
	public boolean isFinal() {
		return state.isFinal;
	}
	public List<Integer> getOutgoingSymbols() {
		List<Integer> symbols = new ArrayList<Integer>();
		for(Integer labelRegister : state.getOutgoingRegisters()) {
			if(labelRegister < a.getWriteableOffset())
				symbols.add(labelRegister);
			else
				symbols.add(registers[labelRegister - a.getWriteableOffset()]);
		}
		return symbols;
	}
	public boolean isDead() {
		return dead;
	}
	
	//Stats
	public static void yieldStatistics(String sessionName, ResultsContainer rc) {
		if(Testbench.COLLECT_STATS) {
			rc.addSessionNumber(sessionName, "nodes", nodesExpanded);
			nodesExpanded=0;
		}
	}
}
