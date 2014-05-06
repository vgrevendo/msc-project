package automata.gen;

/**
 * <p>Generates a diamond-chain-shaped register automaton.
 * All parameters go through slackening unless specified.
 * All states can be final (see parameter).</p>
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
public class DiamondChainGenerator extends AutomatonGenerator {
	//Instance parameters
	public int targetDepth = 100;
	public int targetBranchingFactor = 5;
	public int targetRegisters = 10;
	
	public double fProportion = 0.05;
	
	@Override
	protected void build() {
		//Init registers
		int actualRegisters = slackChoose(targetRegisters);
		for(int r = 0; r < actualRegisters; r++) {
			addRegister(-1);
		}
		
		//Initial state
		addState(0, pickFrom(actualRegisters), false);
		
		//Generate diamonds
		int cStateNumber = 1;
		for(int d = 0; d < targetDepth; d++) {
			//Choose number of intermediate states
			int intermediaryStates = slackChoose(targetBranchingFactor);
			
			//Create target state
			addState(cStateNumber+intermediaryStates, pickFrom(actualRegisters), Math.random() < fProportion);
			
			//Create intermediary states and add transitions
			// (one incoming, one outgoing per state)
			for(int s = 0; s < intermediaryStates; s++) {
				addState(cStateNumber+s, pickFrom(actualRegisters), Math.random() < fProportion);
				
				addTransition(cStateNumber-1, cStateNumber+s, 				   pickFrom(actualRegisters));
				addTransition(cStateNumber+s, cStateNumber+intermediaryStates, pickFrom(actualRegisters));
			}
			
			cStateNumber += intermediaryStates+1;
		}
	}

}
