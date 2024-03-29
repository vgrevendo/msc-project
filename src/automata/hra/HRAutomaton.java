package automata.hra;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import automata.RegisterAutomaton;
import automata.State;

/**
 * A register automaton with a nice heuristic score based on transition
 * label relaxation: what is the number of paths of length n that go from
 * state s to a final state?
 * @author vincent
 *
 */
public class HRAutomaton extends RegisterAutomaton {
	//Internal codes
	private static final int NONE = 0;
	private static final int FINAL = 1;
	
	//Storage
	private final Map<State, int[]> hScores;
	private final int hLength;

	public HRAutomaton(String loadPath, int hlength) throws FileNotFoundException,
			ParseException {
		super(loadPath);
		
		hScores = new HashMap<>();
		hLength = hlength+1;
		
		for(State s : states) {
			hScores.put(s, new int[hLength]);
		}
		
	}
	
	public HRAutomaton(RegisterAutomaton ra, int hlength) {
		super(ra);
		
		hScores = new HashMap<>();
		hLength = hlength+1;
		
		for(State s : states) {
			hScores.put(s, new int[hLength]);
		}
		
	}
	
	/**
	 * Load all hscores by iterating over n, the considered
	 * path distance
	 */
	public void loadHeuristic() {
		//Process final states
		for(State s: states) {
			hScores.get(s)[0] = s.isFinal ? FINAL : NONE;
		}
		
		//Process all states
		for(int n = 1; n < hLength; n++) {
			for(State s: states) {
				hScores.get(s)[n] = computeHScore(s, n);
			}
		}
	}
	
	private int computeHScore(State s, int n) {
		int score = 0;
		
		Map<Integer, List<State>> transitions = this.getTransitions(s);
		
		//For each transition
		for(Entry<Integer, List<State>> tr: transitions.entrySet()) {
			//For each transition's end state
			for(State endState: tr.getValue()) {
				int add = getHScore(endState, n-1);
				score = Integer.MAX_VALUE - score > add ? score + add : Integer.MAX_VALUE;
			}
		}
		
		return score;
	}
	
	/**
	 * Returns f_s(n), where s is the considered state and
	 * n is the expected physical distance from a final state.
	 * @param s
	 * @param n
	 * @return
	 */
	public int getHScore(State s, int n) {
		return hScores.get(s)[n];
	}

}
