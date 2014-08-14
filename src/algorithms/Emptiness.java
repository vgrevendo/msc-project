package algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import testbench.Testbench;
import algorithms.emptiness.EMPDecisionAlgorithm;
import algorithms.emptiness.SearchState;
import algorithms.tools.ResultsContainer;
import automata.RegisterAutomaton;

/**
 * A bunch of algorithms designed to check if an automaton defines
 * an empty language or not.
 * @author vincent
 *
 */
public class Emptiness {
	/**
	 * DFS implementation. In case of no cycles, because low memory consumption.
	 */
	public static final EMPDecisionAlgorithm generativeDFSCheck = new EMPDecisionAlgorithm("DFS") {
		private int maxFrontierSize = 0;
		
		@Override
		public boolean decide(RegisterAutomaton automaton) {
			int[] fullAssignment = buildFullAssignment(automaton);
			SearchState initialState = new SearchState(automaton.getInitialState(), automaton.getInitialRegisters(), 
													   automaton, fullAssignment);
			
			Stack<SearchState> frontier = new Stack<>();
			frontier.add(initialState);
			
			while(!frontier.isEmpty()) {
				SearchState state = frontier.pop();
				
				if(state.isFinal()) {
					if(Testbench.DEBUG)
						System.out.println("Successful configuration: " + state.toString());
					return true;
				}
				
				List<SearchState> adjacentStates = state.expand();
				frontier.addAll(adjacentStates);
				
				if(Testbench.COLLECT_STATS)
					maxFrontierSize = Math.max(maxFrontierSize, frontier.size());
			}
			
			return false;
		}
		@Override
		protected void yieldStatistics(String sessionName, ResultsContainer rc) {
			rc.addSessionNumber(sessionName, "frontier size", maxFrontierSize);
			
			maxFrontierSize = 0;
		}
	};
	
	public static final EMPDecisionAlgorithm generativeBFGSCheck = new EMPDecisionAlgorithm("BFGS") {
		private int maxFrontierSize = 0;
		private int ignored = 0;
		
		@Override
		public boolean decide(RegisterAutomaton automaton) {
			Set<SearchState> visitedStates = new HashSet<>();
			int[] fullAssignment = buildFullAssignment(automaton);
			SearchState initialState = new SearchState(automaton.getInitialState(), automaton.getInitialRegisters(), 
													   automaton, fullAssignment);
			
			Queue<SearchState> frontier = new LinkedList<>();
			frontier.add(initialState);
			visitedStates.add(initialState);
			
			while(!frontier.isEmpty()) {
				SearchState state = frontier.poll();
				
				if(state.isFinal()) {
					if(Testbench.DEBUG)
						System.out.println("Successful configuration: " + state.toString());
					return true;
				}
				
				List<SearchState> adjacentStates = state.expand();
				
				for(SearchState ss: adjacentStates) {
					if(!visitedStates.contains(ss))
						frontier.add(ss);
					else if(Testbench.COLLECT_STATS)
						ignored++;
				}
				
				if(Testbench.COLLECT_STATS)
					maxFrontierSize = Math.max(maxFrontierSize, frontier.size());
			}
			
			return false;
		}
		@Override
		protected void yieldStatistics(String sessionName, ResultsContainer rc) {
			rc.addSessionNumber(sessionName, "frontier size", maxFrontierSize);
			rc.addSessionNumber(sessionName, "ignored nodes", ignored);
			
			maxFrontierSize = 0;
			ignored = 0;
		}
	};
	
	//Tools
	/**
	 * We will avoid 0 here
	 * @param ra
	 * @return
	 */
	public static int[] buildFullAssignment(RegisterAutomaton ra) {
		Map<Integer, Integer> oldToNewSymbols = new HashMap<Integer, Integer>();
		int counter = 1;
		int[] regs = ra.getInitialRegisters();
		
		
		for(int i = 0; i < regs.length; i++) {
			if(regs[i] >= 0) 
				oldToNewSymbols.put(regs[i], counter++);
		}
		
		for(int r = 0; r < regs.length; r++) {
			if(regs[r] >= 0) {
				regs[r] = oldToNewSymbols.get(regs[r]);
			} else {
				regs[r] = counter++;
			}
		}
		
		return regs;
	}
}
