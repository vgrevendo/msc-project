package automata.ldfts;

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
}
