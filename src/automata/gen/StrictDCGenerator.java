package automata.gen;

/**
 * <p>Generates a strict diamond-chain-shaped register automaton.
 * Least slacking possible here!
 * Only the last state is final.
 * The penultimate state is not self-repeating</p>
 * @author vincent
 *
 */
public class StrictDCGenerator extends AutomatonGenerator {
	//Instance parameters
	private final int n;
	
	public StrictDCGenerator(int n) {
		super("Strict Diamond Chain Generator");
		this.n = n;
	}
	
	@Override
	protected void build() {
		//Now building an automaton with n diamonds.
		//Add registers
		addRegister(-1);
		addRegister(-1);
		
		//Build entrance
		addState(0, 0, false);
		addState(1, -1, false);
		
		addTransition(0, 1, 0);
		
		//Set initial state
		setInitialState(0);
		
		//Build n diamonds
		int stateIndex = 2;
		for(int k = 0; k < n; k++) {
			addState(stateIndex, -1, false);
			addState(stateIndex+1, -1, false);
			addState(stateIndex+2, k == n-1 ? 1 : -1, false);
			
			addTransition(stateIndex-1, stateIndex  , 0);
			addTransition(stateIndex-1, stateIndex+1, 0);
			addTransition(stateIndex  , stateIndex+2, 0);
			addTransition(stateIndex+1, stateIndex+2, 0);

			stateIndex += 3;
		}
		
		//Add exit
		addState(stateIndex, -1, true);
		addTransition(stateIndex-1, stateIndex, 1);
	}

}
