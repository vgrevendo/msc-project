package algorithms.emptiness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
	
	
	public SearchNode(SearchState state, SearchNode parent) {
		this.state = state;
		this.parent = parent;
	}
	
	/**
	 * Print the solution path to the results container
	 */
	public void printPath() {
		Stack<SearchNode> path = new Stack<>();
		Stack<Integer> wStack = new Stack<>();
		
		SearchNode currentNode = this;
		while(currentNode != null) {
			path.add(currentNode);
			
			int symbol = currentNode.state.trSymbol;
			if(symbol >= 0)
				wStack.add(symbol);
			
			currentNode = currentNode.parent;
		}
		
		//Transform the symbol stack into a list
		List<Integer> wList = new ArrayList<>(wStack);
		Collections.reverse(wList);
		
		ResultsContainer rc = ResultsContainer.getContainer();
		rc.println("> Solution path (" + path.size() + " steps) for word " + wList.toString() + ":");
		do {
			currentNode = path.pop();
			
			rc.println("  State: " + currentNode.state.state.name + (currentNode.state.isFinal() ? " (FINAL)" : ""));
		} while(!path.isEmpty());
		
		rc.commit();
	}

}
