package algorithms.membership;

import java.util.HashSet;
import java.util.PriorityQueue;

import automata.hra.HRAutomaton;

/**
 * The customised frontier for A* based on physical distance heuristic.
 * This frontier keeps search states ordered, unique, and also won't accept
 * any that have a zero paths number.
 * @author vincent
 *
 */
public class PrioritySet {
	private final HRAutomaton automaton;
	private final HashSet<SearchNode> set;
	private final PriorityQueue<SearchNode> queue;

	public PrioritySet(HRAutomaton automaton) {
		this.automaton = automaton;
		set = new HashSet<>();
		queue = new PriorityQueue<>(automaton.getStates().length, automaton);
	}
	
	public SearchNode pop() {
		SearchNode s = queue.poll();
		set.remove(s);
		return s;
	}
	
	public void add(SearchNode s) {
		if(automaton.getHScore(s.state.state, s.state.w.size()) > 0 && !set.contains(s)) {
			set.add(s);
			queue.add(s);
		}
	}
	
	public boolean isEmpty() {
		return set.isEmpty();
	}

	public int size() {
		return set.size();
	}
}
