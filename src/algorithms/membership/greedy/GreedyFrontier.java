package algorithms.membership.greedy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The frontier that implements the behaviour for greedy search.
 * @author vincent
 *
 */
public class GreedyFrontier {
	//unstables
	public final Set<GreedyConfiguration> unstables;
	//stables
	public final Set<GreedyConfiguration> onRhoStates;
	public final Map<Integer, Set<GreedyConfiguration>> symbolNeeders;
	
	//Monitoring information
	private int size = 0;
	
	public GreedyFrontier() {
		unstables = new HashSet<>();
		onRhoStates = new HashSet<>();
		symbolNeeders = new HashMap<>();
	}
	
	//Read&Write
	public void add(GreedyConfiguration gc) {
		//If unstable, add to unstables
		if(!gc.state.isStable()) {
			unstables.add(gc);
			size++;
			return;
		}
		
		//If stable with s-outgoing rho, add to onRho
		if(gc.state.isRhoCompatible()) {
			onRhoStates.add(gc);
			size++;
			return;
		}
		
		//If s-outgoing symbols
		for(Integer s : gc.getOutgoingSymbols()) {
			if(!symbolNeeders.containsKey(s))
				symbolNeeders.put(s, new HashSet<GreedyConfiguration>());
			symbolNeeders.get(s).add(gc);
			size++;
		}
	}
	public Iterable<GreedyConfiguration> filter(Integer symbol) {
		return new ConfigurationFilter(symbol);
	}
	/**
	 * Copy the contents of the other frontier into this one.
	 * @param otherFrontier
	 */
	public void absorb(GreedyFrontier otherFrontier) {
		unstables.addAll(otherFrontier.unstables);
		onRhoStates.addAll(otherFrontier.onRhoStates);
		
		for(Entry<Integer, Set<GreedyConfiguration>> e: otherFrontier.symbolNeeders.entrySet()) {
			if(!symbolNeeders.containsKey(e.getKey()))
				symbolNeeders.put(e.getKey(), e.getValue());
			else
				symbolNeeders.get(e.getKey()).addAll(e.getValue());
		}
	}
	
	//Tools
	public class ConfigurationFilter implements Iterable<GreedyConfiguration> {
		private final Integer nextSymbol;
		
		public ConfigurationFilter(Integer symbol) {
			this.nextSymbol = symbol;
		}

		@Override
		public Iterator<GreedyConfiguration> iterator() {
			
			return new Iterator<GreedyConfiguration>() {
				private List<Set<GreedyConfiguration>> collections;
				private Iterator<GreedyConfiguration> currentIt;
				private GreedyConfiguration nextItem;

				@Override
				public boolean hasNext() {
					if(collections == null) {
						collections = new LinkedList<>();
						Set<GreedyConfiguration> snSet = symbolNeeders.get(nextSymbol);
						if(snSet != null)
							collections.add(snSet);
						collections.add(onRhoStates);
						
						currentIt = unstables.iterator();
					}
					
					//Discard dead configurations, as these were already considered in the past
					while(true) {
						if(currentIt.hasNext()) {
							//Current iterator is not empty, but are components alive?
							nextItem = currentIt.next();
							currentIt.remove();
							
							if(!nextItem.isDead())
								return true;
						} else {
							//Current iterator is empty
							if(collections.size()==0)
								return false;
							else {
								Set<GreedyConfiguration> nextCollection = collections.get(0);
								collections.remove(0);
								currentIt = nextCollection.iterator();
							}
						}
					}
				}

				@Override
				public GreedyConfiguration next() {
					return nextItem;
				}

				@Override
				public void remove() {}
			};
		}
		
	}
	public int size() {
		return size;
	}
	public int strictSize() {
		return size;
	}
	public boolean isEmpty() {
		return size == 0;
	}
}
