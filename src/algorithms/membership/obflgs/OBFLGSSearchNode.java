package algorithms.membership.obflgs;

import java.util.Stack;

import algorithms.tools.ResultsContainer;

/**
 * <p>Just a wrapper around a search state to remember the path.
 * <span style="color:red;">This is BFLGS specific</span>.</p>
 * @author vincent
 *
 */
public class OBFLGSSearchNode {
	public final OBFLGSSearchState state;
	public final OBFLGSSearchNode parent;
	
	public OBFLGSSearchNode(OBFLGSSearchState state, OBFLGSSearchNode parent) {
		this.state = state;
		this.parent = parent;
	}
	
	/**
	 * Print the solution path to the results container
	 */
	public void printPath() {
		Stack<OBFLGSSearchNode> path = new Stack<>();
		
		OBFLGSSearchNode currentNode = this;
		while(currentNode != null) {
			path.add(currentNode);
			currentNode = currentNode.parent;
		}
		
		ResultsContainer rc = ResultsContainer.getContainer();
		rc.println("> Solution path (" + path.size() + " steps) for word " + path.peek().state.fullWord.toString() + ":");
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
		OBFLGSSearchNode other = (OBFLGSSearchNode) obj;
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
