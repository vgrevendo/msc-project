package automata.gen;

/**
 * Given n, generate a tree automaton with one register and all rhos defined.
 * The root of this tree has n branches of depth n. All branches are "single".
 * All branches terminate in a final state.
 * 
 * This automaton is supposed to show that DFS and A* are asymptotically better than 
 * BFLGS and Best-First.
 * @author vincent
 *
 */
public class RootBranchGenerator extends AutomatonGenerator {
	private final int n;

	public RootBranchGenerator(int n) {
		super("Root Branch Generator");
		this.n = n;
	}

	@Override
	protected void build() {
		//Init registers
		addRegister(-1);
		
		//Initial state
		addState(0, 0, false);
		
		//Construct branches
		for(int b = 0; b < n; b++) {
			constructBranch(b);
		}
	}
	
	private void constructBranch(int number) {
		//Create states and add transitions
		for(int s = 0; s < n; s++) {
			addState(1 + number*n + s, 0, s == n-1);
			addTransition(s == 0 ? 0 : number*n + s, 1 + number*n + s, 0);
		}
	}

}
