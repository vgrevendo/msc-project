package automata.gen;

/**
 * Generates a basic RA by following creating a
 */
public class RandomGenerator extends AutomatonGenerator {
	//Parameters with default values
	public int targetNumStates = 1000;
	public int targetNumTransitions = 7000;
	
	public int numRegisters = 20;

	public double definedRhoProportion = 0.5;
	public double labelIsRhoProportion = 0.7;
	
	/**
	 * The proportion of expected number of final states
	 */
	public double fProportion = 0.05;
	
	public RandomGenerator() {
		super("Random Automaton Generator");
	}
	
	
	@Override
	protected void build() {
		//Init registers
		int actualNumRegisters = slackChoose(numRegisters);
		
		//Describe states
		int actualNumStates = slackChoose(targetNumStates);
		int[] rhos = new int[actualNumStates];
		
		//Add initial state (defines rho)
		addState(0, rhos[0] = pickFrom(actualNumRegisters-1), false);
		
		for(int s = 1; s < actualNumStates; s++) {
			boolean hasRho = Math.random() < definedRhoProportion;
			boolean isFinal = Math.random() < fProportion;
			
			rhos[s] = pickFrom(actualNumRegisters-1);
			addState(s, hasRho ? rhos[s] : -1, isFinal);
		}
		
		//Describe registers: only empty ones
		for(int r = 0; r < actualNumRegisters-1; r++) {
			addRegister(-1);
		}
		
		//Describe transitions
		int actualNumTransitions = slackChoose(targetNumTransitions);
		
		for(int t = 0; t < actualNumTransitions; t++) {
			//For each transition, choose a random source state
			//and a random destination state, with a random register
			//and a random containing label
			int s1 = pickFrom(actualNumStates);
			int s2 = pickFrom(actualNumStates);
			int label = (Math.random() < labelIsRhoProportion ? 
					rhos[s1] :
					pickFrom(actualNumRegisters-1));
					
			addTransition(s1, s2, label);
		}
				
	}
	
}
