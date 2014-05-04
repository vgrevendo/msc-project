package algorithms;

import java.util.HashSet;

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
		
		Integer[] sequence = computeMinimalSubset(a);
		
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
	 * @param a
	 * @return
	 */
	public static boolean generativeCompleteEmptinessCheck(RegisterAutomaton a) {
		return false;
	}
	
	/**
	 * As explained in the PROPOSITION, it is only necessary to work with a small subset 
	 * of the infinite alphabet. This method computes a list representing that subset.
	 * @param a
	 * @return
	 */
	public static Integer[] computeMinimalSubset(RegisterAutomaton a) {
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
