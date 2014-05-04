package algorithms;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import algorithms.bfgs.SearchNode;
import algorithms.bfgs.SearchState;
import algorithms.emptiness.WordIterator;
import automata.RegisterAutomaton;

/**
 * A bunch of algorithms designed to check if an automaton defines
 * an empty language or not.
 * @author vincent
 *
 */
public class Emptiness {
	/**
	 * Test all words within a certain finite subset of the alphabet and
	 * within a length. This gives an idea as to whether the language is empty or not.
	 * 
	 * First step is to construct the useful alphabet subset that we will need. Then iterate
	 * over all possibilities for membership checks.
	 */
	public static boolean empiricalEmptinessCheck(RegisterAutomaton a, int wordLengthLimit) {
		boolean deterministic = Tools.isDeterministic(a);
		
		Integer[] sequence = computeMinimalAlphabet(a);
		
		System.out.println("EMPIRICAL EMPTINESS CHECK");
		System.out.println("Alphabet subset size is " + sequence.length);
		
		
		for(int wordSize = 1; wordSize < wordLengthLimit; wordSize++) {
			WordIterator wi = new WordIterator(wordSize, sequence);
			
			System.out.println("Checking wordsize " + wordSize + " (" + wi.capacity + " words)");
			
			for(int[] word : wi) {
				boolean member = deterministic ? 
									Membership.deterministicMemberCheck(a, word) :
									Membership.nondeterministicMemberCheck(a, word);
				if(member)
					return true;
			}
			
		}
		
		
		return false;
	}
	
	/**
	 * The generative and complete version of the previous empirical emptiness check.
	 * Returns true if the automaton is nonempty, false if the automaton is empty.
	 * @param a
	 * @return
	 */
	public static boolean generativeCompleteEmptinessCheck(RegisterAutomaton a) {
		Set<SearchState> visitedStates = new HashSet<>();
		SearchState initialState = new SearchState(a.getInitialState(), 
												   a.getInitialRegisters(), 
												   a, 
												   -1);
		visitedStates.add(initialState);
		
		//For BF(G)S we'll need a FIFO data structure. 
		Queue<SearchNode> frontier = new LinkedList<>();
		SearchNode initialNode = new SearchNode(initialState, null);
		frontier.add(initialNode);
		
		while(!frontier.isEmpty()) {
			SearchNode currentNode = frontier.poll();
			
			if(currentNode.state.isFinal()) {
				currentNode.printPath();
				return true;
			}
			
			List<SearchState> adjacentStates = currentNode.state.expand();
			for(SearchState aState : adjacentStates) {
				if(!visitedStates.contains(aState)) {
					visitedStates.add(aState);
					frontier.add(new SearchNode(aState, currentNode));
				}
			}
		}
		
		return false;
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
