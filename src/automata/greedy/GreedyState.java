package automata.greedy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * A state with more autonomy than before, and more optimisations.
 * @author vincent
 *
 */
public class GreedyState {
	//Identity
	public final String name;
	private final int hc;
	
	//Characteristics
	public final boolean isFinal;
	private boolean stable = false;
	private boolean rhoCompatible = false;
	private List<Integer> outgoingLabels;
	
	//Environment
	private final Map<Integer, List<GreedyState>> mu;
	private int rho; //TODO make final
	
	public GreedyState(String name, boolean isFinal, int rho) {
		this.name = name;
		this.isFinal = isFinal;
		this.rho = rho;
		
		this.mu = new HashMap<>();
		this.outgoingLabels = new ArrayList<>();
		
		this.hc = name.hashCode();
	}
	
	//Accessors
	public boolean isStable() {
		return stable;
	}
	public boolean isRhoCompatible() {
		return rhoCompatible;
	}
	public List<Integer> getOutgoingRegisters() {
		return outgoingLabels;
	}
	public int getAssignmentRegister() {
		return rho;
	}
	public List<GreedyState> getNextStates(Integer label) {
		return mu.get(label);
	}
	
	//TODO To be replaced by something intelligent
	public void setRho(int newRho) {
		this.rho = newRho;
	}
	public void clearMu() {
		mu.clear();
		outgoingLabels.clear();
	}
	public Map<Integer, List<GreedyState>> getTransitions() {
		return mu;
	}
	
	//Tools
	public void computeCharacteristics(int numRegisters, int writeOffset) {
		//Both characteristics depend on the presence of a rho value
		if(rho < 0)
			return;
		
		//Compute stability: count number of unique-label transitions
		Set<Integer> labels = new HashSet<>();
		for(Entry<Integer, List<GreedyState>> e : mu.entrySet()) {
			labels.add(e.getKey());
		}
		
		//And compare to number of registers
		stable = labels.size() >= numRegisters;
		
		//Compute rho compatibility
		if(mu.containsKey(Integer.valueOf(rho+writeOffset)))
			for(GreedyState s : mu.get(Integer.valueOf(rho+writeOffset))) 
				if(s != this) {
					rhoCompatible = true;
					return;
				}
		
		rhoCompatible = false;
	}
	public void addTransition(Integer label, GreedyState state) {
		//Update mu
		if(!mu.containsKey(label))
			mu.put(label, new ArrayList<GreedyState>());
		mu.get(label).add(state);

		if(state != this) //If the transition is strictly outgoing
			outgoingLabels.add(label);
	}

	
	//Generics
	@Override
	public int hashCode() {
		return hc;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GreedyState other = (GreedyState) obj;
		return !name.equals(other.name);
	}

	@Override
	public String toString() {
		return name;
	}
	
}
