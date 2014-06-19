package algorithms.membership;

import java.util.List;
import java.util.RandomAccess;

import algorithms.tools.ResultsContainer;
import automata.Automaton;

/**
 * An abstract class to make test extensions easier.
 * @author vincent
 *
 */
public abstract class MBSDecisionAlgorithm {
	private Automaton ra;
	protected final ResultsContainer rc;
	public final String name;
	
	public MBSDecisionAlgorithm(Automaton ra, String name) {
		setAutomaton(ra);
		rc = ResultsContainer.getContainer();
		rc.createSession(name);
		this.name = name;
	}
	
	public MBSDecisionAlgorithm(String name) {
		this.ra = null;
		rc = ResultsContainer.getContainer();
		rc.createSession(name);
		this.name = name;
	}
	
	public void setAutomaton(Automaton ra) {
		this.ra = ra;
	}
	
	public boolean decide(List<Integer> word) {
		if(!(word instanceof RandomAccess))
			System.err.println("WARNING: " + name + " received slow-access list, algorithm will perform badly");
		return decide(ra, word);
	}
	
	public abstract boolean decide(Automaton automaton, List<Integer> word);

	@Override
	public String toString() {
		return name;
	}

	//Statistics
	public void yieldStatistics(ResultsContainer rc) {
		yieldStatistics(name, rc);
	}
	protected abstract void yieldStatistics(String sessionName, ResultsContainer rc);
}
