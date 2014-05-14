package automata.gen;

/**
 * <p>Generates a strict diamond-chain-shaped register automaton.
 * Least slacking possible here!
 * Only the last state is final.
 * The penultimate state is a self-repeating state as in
 * example 4</p>
 * 
 * <p>Possible improvements include:
 * <ul>
 * <li>Have more incoming/outgoing transitions per intermediary state</li>
 * <li>Do not choose labels randomly but with high probability for rho</li>
 * <li>Do not always define rho?</li>
 * </ul>
 * </p>
 * @author vincent
 *
 */
public class StrictDCGenerator extends AutomatonGenerator {
	//Instance parameters
	public int targetDepth = 5;
	public int branchingFactor = 2;
	public int registers = 2;
	
	public double labelIsRhoProportion = 0.7;
	
	public StrictDCGenerator() {
		super("Strict Diamonds Chain Generator");
	}
	
	@Override
	protected void build() {
		int intermediaryStates = branchingFactor;
		
		//Init registers
		int actualRegisters = registers;
		for(int r = 0; r < actualRegisters; r++) {
			addRegister(-1);
		}
		
		//Initial state
		addState(0, 0, false);
		
		//Generate diamonds
		int cStateNumber = 1;
		for(int d = 0; d < targetDepth; d++) {
			//Create target state
			addState(cStateNumber+intermediaryStates, 
					 0, 
					 false);
			
			//Create intermediary states and add transitions
			// (one incoming, one outgoing per state)
			for(int s = 0; s < intermediaryStates; s++) {
				addState(cStateNumber+s, 
						 0, 
						 false);
				
				addTransition(cStateNumber-1, 
							  cStateNumber+s, 				   
							  0);
				addTransition(cStateNumber+s, 
							  cStateNumber+intermediaryStates, 
							  0);
			}
			
			cStateNumber += intermediaryStates+1;
		}
		
		//Penultimate state
		addState(cStateNumber, 1, false);
		addTransition(cStateNumber, cStateNumber, 0);
		addTransition(cStateNumber, cStateNumber, 0);
		
		//Final state
		addState(cStateNumber+1, -1, true);
		addTransition(cStateNumber, cStateNumber+1, 1);
	}

}
