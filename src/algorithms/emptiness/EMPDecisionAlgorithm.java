package algorithms.emptiness;

import algorithms.tools.ResultsContainer;
import automata.RegisterAutomaton;

/**
 * An abstract class to make test extensions easier.
 * @author vincent
 *
 */
public abstract class EMPDecisionAlgorithm {
	protected final ResultsContainer rc;
	public final String name;
	
	public EMPDecisionAlgorithm(String name) {
		rc = ResultsContainer.getContainer();
		rc.createSession(name);
		this.name = name;
	}
	
	public abstract boolean decide(RegisterAutomaton automaton);

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
