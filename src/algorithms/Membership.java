package algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import algorithms.membership.MBSDecisionAlgorithm;
import algorithms.membership.PrioritySet;
import algorithms.membership.SearchNode;
import algorithms.membership.SearchState;
import algorithms.tools.ResultsContainer;
import automata.RegisterAutomaton;
import automata.State;
import automata.hra.HRAutomaton;

public class Membership {
	/**
	  * A membership check which will first determine whether the automaton
	  * is deterministic, then delegate to an already existing method.
	  */
	public static final MBSDecisionAlgorithm intelligentCheck = new MBSDecisionAlgorithm("Intelli-mbs") {

		@Override
		public boolean decide(RegisterAutomaton automaton, int[] word) {
			if(Tools.isDeterministic(automaton))
				return deterministicCheck.decide(automaton, word);
			
			return ldftsCheck.decide(automaton, word);
		}
	};
	
	 /**
	   * A tiny method that needs a deterministic automaton in order to function (faster
	   * and easier to implement)
	   */
	public static final MBSDecisionAlgorithm deterministicCheck = new MBSDecisionAlgorithm("Det-mbs") {
		
		@Override
		public boolean decide(RegisterAutomaton automaton, int[] word) {
			State currentState = automaton.getInitialState();
			int[] registers = automaton.getInitialRegisters();
			int containingRegister = 0;
			Integer assignmentRegister = 0;
			
			//Simply follow the path
			for(int wcursor = 0; wcursor < word.length; wcursor++) {
				//If our registers do not contain the current symbol
				if((containingRegister = Tools.registersContain(registers, word[wcursor])) < 0) {
					//If a rho value is defined
					if((assignmentRegister = automaton.getAssignmentRegister(currentState)) != null) {
						containingRegister = assignmentRegister;
						registers[containingRegister] = word[wcursor];
					}
				}
				
				//Make transition if possible
				List<State> nextStates = automaton.getNextStates(currentState, containingRegister);
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
	};
	
	/**
	 * A first naïve version of nondeterministic membership checking, by performing a
	 * Limited Depth-First Tree Search into the automaton graph; see the ldfts package
	 * for components. DFTS implies a stack as a frontier. 
	 */
	public static final MBSDecisionAlgorithm ldftsCheck = new MBSDecisionAlgorithm("Ldfts-mbs") {
		@Override
		public boolean decide(RegisterAutomaton automaton, int[] word) {
			ResultsContainer rc = ResultsContainer.getContainer();
			int maxFrontierSize = 0;
			int nodesExpanded = 0;
			
			//Convert the word into a list, better for later
			ArrayList<Integer> wl = new ArrayList<>();
			for(int i : word) {
				wl.add(i);
			}
			
			//Depth-first search implies a stack storing the frontier
			Stack<SearchNode> frontier = new Stack<>();
			SearchState initialSearchState = new SearchState(automaton.getInitialState(), 
															 automaton.getInitialRegisters(), 
															 wl, automaton);
			frontier.add(new SearchNode(initialSearchState, null, -1));
			
			//Main search loop
			while(!frontier.isEmpty()) {
				maxFrontierSize = Math.max(maxFrontierSize, frontier.size());
				
				SearchNode node = frontier.pop();
				if(node.state.isFinal()) {
					rc.addNumber(nodesExpanded);
					rc.addNumber(maxFrontierSize);
					return true;
				}
				
				List<SearchState> nextStates = node.state.expand();
				nodesExpanded++;
				
				for(SearchState s : nextStates) {
					frontier.add(new SearchNode(s, node, 0));
				}
			}
			
			rc.addNumber(nodesExpanded);
			rc.addNumber(maxFrontierSize);
			
			return false;
		}
	};
	
	/**
	 * A second naïve version of nondeterministic membership checking, performing a
	 * Breadth-First Local Graph Search into the automaton graph;  
	 * BFLGS implies a double-set structure as a frontier. 
	 */
	public static final MBSDecisionAlgorithm bflgsCheck = new MBSDecisionAlgorithm("Bflgs-mbs") {
		
		@Override
		public boolean decide(RegisterAutomaton automaton, int[] word) {
			ResultsContainer rc = ResultsContainer.getContainer();
			int maxFrontierSize = 0;
			int nodesExpanded = 0;
			
			//Convert the word into a list, better for later
			ArrayList<Integer> wl = new ArrayList<>();
			for(int i : word) {
				wl.add(i);
			}
			
			//BFLGS implies a double set storing the frontier
			List<HashSet<SearchNode>> sets = new ArrayList<>();
			sets.add(new HashSet<SearchNode>());
			sets.add(new HashSet<SearchNode>());
			int activeSet = 0;
			
			Set<SearchNode> frontier = sets.get(activeSet);
			
			//Initial state
			SearchState initialSearchState = new SearchState(automaton.getInitialState(), 
															 automaton.getInitialRegisters(), 
															 wl, automaton);
			frontier.add(new SearchNode(initialSearchState, null, -1));
			
			//Main search loop
			while(!frontier.isEmpty()) {
				maxFrontierSize = Math.max(maxFrontierSize, frontier.size());

				//See if a node is final, and 
				// start filling up the next frontier
				activeSet = (activeSet+1)%2;
				Set<SearchNode> nextFrontier = sets.get(activeSet);
				for(SearchNode node: frontier) {
					if(node.state.isFinal()) {
						rc.addNumber(nodesExpanded);
						rc.addNumber(maxFrontierSize);
						return true;
					}

					//Add the adjacent nodes to the new frontier
					List<SearchState> nextStates = node.state.expand();
					nodesExpanded++;
					
					for(SearchState s : nextStates) {
						nextFrontier.add(new SearchNode(s, node, 0));
					}
				}

				//Then start over with the next frontier
				frontier.clear();
				frontier = nextFrontier;
			}
			
			rc.addNumber(nodesExpanded);
			rc.addNumber(maxFrontierSize);
			
			return false;
		}
	};

	/**
	 * A third less naive version, using the physical distance heuristic.
	 */
	public static final MBSDecisionAlgorithm bestFirstCheck = new MBSDecisionAlgorithm("Best-first-mbs") {
		private Comparator<SearchNode> comparator;
		private HRAutomaton a;
		
		@Override
		public boolean decide(RegisterAutomaton automaton, int[] word) {
			//Ignore the automaton given as an argument, we're going to use the one stored
			ResultsContainer rc = ResultsContainer.getContainer();
			int maxFrontierSize = 0;
			int nodesExpanded = 0;
			
			//Convert the word into a list, better for later
			ArrayList<Integer> wl = new ArrayList<>();
			for(int i : word) {
				wl.add(i);
			}
			
			//A* employs a heuristic-driven queue
			PrioritySet frontier = new PrioritySet(a, comparator);
			SearchState initialSearchState = new SearchState(a.getInitialState(), 
															 a.getInitialRegisters(), 
															 wl, a);
			frontier.add(new SearchNode(initialSearchState, null, -1));
			
			//Main search loop
			while(!frontier.isEmpty()) {
				maxFrontierSize = Math.max(maxFrontierSize, frontier.size());
				
				SearchNode node = frontier.pop();
				if(node.state.isFinal()) {
					rc.addNumber(nodesExpanded);
					rc.addNumber(maxFrontierSize);
					return true;
				}
				
				List<SearchState> nextStates = node.state.expand();
				nodesExpanded++;
				
				for(SearchState s : nextStates) {
					frontier.add(new SearchNode(s, node, 0));
				}
			}
			
			rc.addNumber(nodesExpanded);
			rc.addNumber(maxFrontierSize);
			
			return false;
		}
		
		@Override
		public void setAutomaton(RegisterAutomaton ra) {
			this.a = (HRAutomaton) ra;
			
			System.out.print("(building HRA heuristic) ");
			a.loadHeuristic();

			this.comparator = new Comparator<SearchNode>() {

				@Override
				public int compare(SearchNode o1, SearchNode o2) {
					return a.getHScore(o1.state.state, o1.state.w.size()) - a.getHScore(o2.state.state, o2.state.w.size());
				}
			};
		}
	};

	/**
	 * The same as above but with a different comparator (and therefore a different heuristic).
	 * Now there is a lexicographical comparison between (-distance, hscore diff): we always first
	 * want to explore the path that is closest to the final state). 
	 */
	public static final MBSDecisionAlgorithm aStarCheck = new MBSDecisionAlgorithm("~A*-mbs") {
		private Comparator<SearchNode> comparator;
		private HRAutomaton a;
		
		@Override
		public boolean decide(RegisterAutomaton automaton, int[] word) {
			//Ignore the automaton given as an argument, we're going to use the one stored
			ResultsContainer rc = ResultsContainer.getContainer();
			int maxFrontierSize = 0;
			int nodesExpanded = 0;
			
			//Convert the word into a list, better for later
			ArrayList<Integer> wl = new ArrayList<>();
			for(int i : word) {
				wl.add(i);
			}
			
			//A* employs a heuristic-driven queue
			PrioritySet frontier = new PrioritySet(a, comparator);
			SearchState initialSearchState = new SearchState(a.getInitialState(), 
															 a.getInitialRegisters(), 
															 wl, a);
			frontier.add(new SearchNode(initialSearchState, null, -1));
			
			//Main search loop
			while(!frontier.isEmpty()) {
				maxFrontierSize = Math.max(maxFrontierSize, frontier.size());
				
				SearchNode node = frontier.pop();
				if(node.state.isFinal()) {
					rc.addNumber(nodesExpanded);
					rc.addNumber(maxFrontierSize);
					return true;
				}
				
				List<SearchState> nextStates = node.state.expand();
				nodesExpanded++;
				
				for(SearchState s : nextStates) {
					frontier.add(new SearchNode(s, node, 0));
				}
			}
			
			rc.addNumber(nodesExpanded);
			rc.addNumber(maxFrontierSize);
			
			return false;
		}
		
		@Override
		public void setAutomaton(RegisterAutomaton ra) {
			this.a = (HRAutomaton) ra;
			
			System.out.print("(building HRA heuristic) ");
			a.loadHeuristic();

			this.comparator = new Comparator<SearchNode>() {

				@Override
				public int compare(SearchNode o1, SearchNode o2) {
					int sizeDiff = o1.state.w.size() - o2.state.w.size();
					
					return sizeDiff == 0 ?							
							a.getHScore(o1.state.state, o1.state.w.size()) - a.getHScore(o2.state.state, o2.state.w.size()) :
							-sizeDiff;
				}
			};
		}
	};
}
