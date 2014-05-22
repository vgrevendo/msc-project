package algorithms.membership;

import java.util.Arrays;
import java.util.Stack;

import algorithms.tools.ResultsContainer;

/**
 * Just a wrapper around a search state to remember the path
 * @author vincent
 *
 */
public class SearchNode {
	public final SearchState state;
	public final SearchNode parent;
	public final int previousTransition;
	
	
	public SearchNode(SearchState state, SearchNode parent, int previousTransition) {
		this.state = state;
		this.parent = parent;
		this.previousTransition = previousTransition;
	}
	
	/**
	 * Print the solution path to the results container
	 */
	public void printPath() {
		Stack<SearchNode> path = new Stack<>();
		
		SearchNode currentNode = this;
		while(currentNode != null) {
			path.add(currentNode);
			currentNode = currentNode.parent;
		}
		
		ResultsContainer rc = ResultsContainer.getContainer();
		rc.println("> Solution path (" + path.size() + " steps) for word " + path.peek().state.w.toString() + ":");
		do {
			currentNode = path.pop();
			
			rc.println("  State: " + currentNode.state.state.name + (currentNode.state.isFinal() ? " (FINAL)" : ""));
		} while(!path.isEmpty());
		
		rc.commit();
	}

	@Override
	public int hashCode() {
		return state.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchNode other = (SearchNode) obj;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return state.toString();
	}
}
