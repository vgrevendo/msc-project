package automata.ldfts;

import java.util.Stack;

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
	
	public void printPath() {
		Stack<SearchNode> path = new Stack<>();
		
		SearchNode currentNode = this;
		while(currentNode != null) {
			path.add(currentNode);
			currentNode = currentNode.parent;
		}
		
		System.out.println("> Solution path:");
		do {
			currentNode = path.pop();
			
			System.out.println("  State: " + currentNode.state.state.name + (currentNode.state.isFinal() ? " (FINAL)" : ""));
		} while(!path.isEmpty());
	}
}
