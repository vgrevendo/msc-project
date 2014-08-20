package automata.gen;

import java.io.FileNotFoundException;

public class DetSATGenerator extends NondetSATGenerator {

	public DetSATGenerator(String filename) throws FileNotFoundException {
		super(filename);
	}
	
	@Override
	protected void buildAutomaton() {
		//Build automaton according to description in fma_npc
		//STATES
		addState(0, 2*n, false);
		for(int i = 1; i <= 2*n; i++) {
			addState(i, i-1, false);
		}
		for(int i = 2*n + 1; i <= 2*n + m; i++) {
			addState(i, -1, false);
		}
		addState(2*n+m+1, -1, true);
		
		//REGSITERS
		for(int i = 0; i < 2*n+1; i++)
			addRegister(-1);
		setInitialState(0);
		
		//TRANSITIONS
		//Delta1
		addTransition(0, 1, 2*n+1-1);
		addTransition(n, 2*n+1, n-1);
		addTransition(2*n, 2*n+1, 2*n-1);
		
		//Delta2
		for(int i = 1; i <= n-1; i++) {
			addTransition(i, i+1, i-1);
			addTransition(i, n+i, 2*n+1-1);
			addTransition(n+i, i+1, n+i-1);
		}
		//Delta3
		for(int i = 1; i <= m; i++) {
			for(int j : clauses.get(i-1)) {
				addTransition(2*n+i, 2*n+i+1, j-1);
			}
		}
	}

}
