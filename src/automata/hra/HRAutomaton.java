package automata.hra;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Arrays;
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
	private static final int UNSET = -1;
	private static final int NONE = 0;
	private static final int FINAL = -2;
	
	//Storage
	private final Map<State, int[]> hScores;
	private final int hLength;

	public HRAutomaton(String loadPath, int hlength) throws FileNotFoundException,
			ParseException {
		super(loadPath);
		
		hScores = new HashMap<>();
		
		for(State s : states) {
			int[] scores = new int[hlength];
			//This step might be avoided by modifying the code above
			Arrays.fill(scores, -1);
			hScores.put(s, scores);
		}
		
		hLength = hlength;
		
		loadHScores();
	}
	
	/**
	 * Load all hscores by iterating over n, the considered
	 * path distance
	 */
	private void loadHScores() {
		//Process final states
		for(State s: states) {
			hScores.get(s)[0] = s.isFinal ? FINAL : NONE;
		}
		
		//Process all states
		for(int n = 0; n < hLength; n++) {
			
		}
	}
	
	private int computeHScore(State s, int n) {
		int score = 0;
		
		Map<Integer, List<State>> transitions = this.getTransitions(s);
		
		//For each transition
		for(Entry<Integer, List<State>> tr: transitions.entrySet()) {
			
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
