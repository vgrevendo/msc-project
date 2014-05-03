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
		
		System.out.println("EMPIRICAL EMPTINESS CHECK");
		System.out.println("There are " + ir.length + " registers");
		System.out.println("Alphabet subset size is " + subset.size());
		
		Integer[] sequence = new Integer[subset.size()];
		subset.toArray(sequence);
		
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
}
