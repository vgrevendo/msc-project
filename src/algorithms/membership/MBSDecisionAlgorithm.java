package algorithms.membership;

import algorithms.tools.ResultsContainer;
import automata.RegisterAutomaton;

/**
 * An abstract class to make test extensions easier.
 * @author vincent
 *
 */
public abstract class MBSDecisionAlgorithm {
	private RegisterAutomaton ra;
	protected final ResultsContainer rc;
	public final String name;
	
	public MBSDecisionAlgorithm(RegisterAutomaton ra, String name) {
		setAutomaton(ra);
		rc = ResultsContainer.getContainer();
		this.name = name;
	}
	
	public MBSDecisionAlgorithm(String name) {
		this.ra = null;
		rc = ResultsContainer.getContainer();
		this.name = name;
	}
	
	public void setAutomaton(RegisterAutomaton ra) {
		this.ra = ra;
	}
	
	public boolean decide(int[] word) {
		return decide(ra, word);
	}
	
	public abstract boolean decide(RegisterAutomaton automaton, int[] word);

	@Override
	public String toString() {
		return name;
	}
}
