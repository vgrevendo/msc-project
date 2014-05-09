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
	public int targetDepth = 1000;
	public int targetBranchingFactor = 2;
	public int targetRegisters = 10;
	
	public double fProportion = 0.5;
	public double labelIsRhoProportion = 0.7;
	
	@Override
	protected void build() {
		//Init registers
		int actualRegisters = slackChoose(targetRegisters);
		for(int r = 0; r < actualRegisters; r++) {
			addRegister(-1);
		}
		
		//Initial state
		int previousRho;
		addState(0, previousRho = pickFrom(actualRegisters), false);
		
		//Generate diamonds
		int cStateNumber = 1;
		for(int d = 0; d < targetDepth; d++) {
			//Choose number of intermediate states
			int intermediaryStates = targetBranchingFactor;
			int[] rhos = new int[intermediaryStates+2];
			rhos[0] = previousRho;
			
			//Create target state
			addState(cStateNumber+intermediaryStates, 
					 rhos[intermediaryStates+1] = pickFrom(actualRegisters-1), 
					 Math.random() < fProportion);
			
			//Create intermediary states and add transitions
			// (one incoming, one outgoing per state)
			for(int s = 0; s < intermediaryStates; s++) {
				addState(cStateNumber+s, 
						 rhos[s+1] = pickFrom(actualRegisters-1), 
						 Math.random() < fProportion);
				
				addTransition(cStateNumber-1, 
							  cStateNumber+s, 				   
							  Math.random() < labelIsRhoProportion ? rhos[0] : pickFrom(actualRegisters-1));
				addTransition(cStateNumber+s, 
							  cStateNumber+intermediaryStates, 
							  Math.random() < labelIsRhoProportion ? rhos[s+1] : pickFrom(actualRegisters-1));
			}
			
			cStateNumber += intermediaryStates+1;
			previousRho = rhos[intermediaryStates+1];
		}
	}

}
