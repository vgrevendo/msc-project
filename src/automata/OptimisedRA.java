package automata;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class OptimisedRA extends RegisterAutomaton {
	private int[] fixedRegisters;
	private int writeOffset;

	public OptimisedRA(String loadPath) throws FileNotFoundException,
			ParseException {
		super(loadPath);
		postBuildOptimise();
	}

	public OptimisedRA(RegisterAutomaton ra) {
		super(ra);
		postBuildOptimise();
	}
	
	private void postBuildOptimise() {
		detectFixedRegisters();
	}
	
	/**
	 * The aim here is to detect whether certain registers are never
	 * assigned. Then registers are regrouped and transitions and rho
	 * values are reassigned in the given order.
	 */
	private void detectFixedRegisters() {
		//Get a set full of read-only register indexes
		Set<Integer> writableRegisters = new HashSet<>();
		Set<Integer> readOnlyRegisters = new HashSet<>();
		
		for(int i = 0; i < registers.length; i++) {
			readOnlyRegisters.add(i);
		}
		
		for(Entry<State, Integer> rhoEntry : rho.entrySet()) {
			if(rhoEntry.getValue() != null) {
				readOnlyRegisters.remove(rhoEntry.getValue());
				writableRegisters.add(rhoEntry.getValue());
			}
		}
		
		//Rewrite registers
		// Rewritemap is a map that maps old indexes to new ones
		Map<Integer, Integer> rewriteMap = new HashMap<Integer, Integer>();
		int[] oldRegisters = registers;
		registers = new int[writableRegisters.size()];
		fixedRegisters = new int[readOnlyRegisters.size()+1];
		writeOffset = readOnlyRegisters.size();
		
		int counter = 0;
		for(Integer i : readOnlyRegisters) {
			rewriteMap.put(i, counter);
			fixedRegisters[oldRegisters[i]] = counter;
			counter++;
		}
		for(Integer i : writableRegisters) {
			rewriteMap.put(i, counter);
			registers[counter-fixedRegisters.length+1] = oldRegisters[i];
			counter++;
		}
		
		//Rewrite rho values
		HashMap<State, Integer> newRho = new HashMap<>();
		for(Entry<State, Integer> rhoEntry: rho.entrySet()) {
			newRho.put(rhoEntry.getKey(), rewriteMap.get(rhoEntry.getValue())-writeOffset);
		}
		rho = newRho;
		
		//Rewrite transitions
		Map<State, Map<Integer, List<State>>> newMu = new HashMap<>();
		for(Entry<State, Map<Integer, List<State>>> muEntry : mu.entrySet()) {
			Map<Integer, List<State>> newValue = new HashMap<Integer, List<State>>();
			newMu.put(muEntry.getKey(), newValue);
			
			for(Entry<Integer, List<State>> e : muEntry.getValue().entrySet()) {
				newValue.put(rewriteMap.get(e.getKey()), e.getValue());
			}
		}
		mu = newMu;
		
		//And we're ready to go.
	}

	
	public int[] getFixedRegisters() {
		return fixedRegisters;
	}
	
	public int findContainingRegister(int[] registers, int symbol) {
		if(symbol > 0 && symbol <= fixedRegisters.length) 
			return fixedRegisters[symbol];
		int i =0;
		for(int s : registers) {
			if(s == symbol)
				return writeOffset + i;
			i++;
		}
		return -1;
	}
	
	public int getWriteableOffset() {
		return writeOffset;
	}

	
}
