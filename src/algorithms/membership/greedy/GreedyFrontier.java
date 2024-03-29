package algorithms.membership.greedy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import algorithms.tools.ResultsContainer;
import testbench.Testbench;

/**
 * The frontier that implements the behaviour for greedy search.
 * @author vincent
 *
 */
public class GreedyFrontier {
	//unstables
	public final List<GreedyConfiguration> unstables;
	//stables
	public final List<GreedyConfiguration> onRhoStates;
	public final Map<Integer, List<GreedyConfiguration>> symbolNeeders;
	
	public int terminalGoals = 0;

	//Statistics collection (see testbench)
	private static int size = 0;
	private static int rhoCompCounter = 0;
	private static int symbolNeedCounter = 0;
	private static int unstableCounter = 0;
	private static int maxActiveSize = 0;
	private static int ignoredConfigs = 0;
	private static int deadConfigs = 0;
	private static int maxSNSize = 0;
	private static int emptyActiveFrontiers = 0;
	
	public GreedyFrontier() {
		unstables = new LinkedList<>();
		onRhoStates = new LinkedList<>();
		symbolNeeders = new HashMap<>();
	}
	
	//Read&Write
	public void add(GreedyConfiguration gc) {
		//If unstable, add to unstables
		if(!gc.state.isStable()) {
			unstables.add(gc);
			size++;
			if(Testbench.COLLECT_STATS)
				unstableCounter++;
			return;
		}
		
		//If stable with s-outgoing rho, add to onRho
		if(gc.state.isRhoCompatible()) {
			onRhoStates.add(gc);
			size++;
			if(Testbench.COLLECT_STATS)
				rhoCompCounter++;
			return;
		}
		
		//If s-outgoing symbols
		boolean needsSymbols = false;
		for(Integer s : gc.getOutgoingSymbols()) {
			if(!symbolNeeders.containsKey(s))
				symbolNeeders.put(s, new LinkedList<GreedyConfiguration>());
			symbolNeeders.get(s).add(gc);
			size++;
			if(Testbench.COLLECT_STATS)
				symbolNeedCounter++;
			needsSymbols = true;
		}
		
		if(needsSymbols)
			return;
		
		if(Testbench.COLLECT_STATS)
			ignoredConfigs++;
		
		if(gc.isFinal()) {
			terminalGoals++;
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
		
		for(Entry<Integer, List<GreedyConfiguration>> e: otherFrontier.symbolNeeders.entrySet()) {
			if(!symbolNeeders.containsKey(e.getKey()))
				symbolNeeders.put(e.getKey(), e.getValue());
			else
				symbolNeeders.get(e.getKey()).addAll(e.getValue());
		}
		
		terminalGoals += otherFrontier.terminalGoals;
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
				private List<List<GreedyConfiguration>> collections;
				private Iterator<GreedyConfiguration> currentIt;
				private GreedyConfiguration nextItem;
				
				//Stats
				private int activeSize = 0;

				@Override
				public boolean hasNext() {
					if(collections == null) {
						collections = new LinkedList<>();
						List<GreedyConfiguration> snSet = symbolNeeders.remove(nextSymbol);
						if(snSet != null) {
							collections.add(snSet);
							if(Testbench.COLLECT_STATS)
								maxSNSize = Math.max(snSet.size(), maxSNSize);
						}
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
							else if(Testbench.COLLECT_STATS)
								deadConfigs++;
						} else {
							//Current iterator is empty
							if(collections.size()==0) {
								if(Testbench.COLLECT_STATS) {
									maxActiveSize = Math.max(activeSize, maxActiveSize);
									emptyActiveFrontiers += activeSize == 0 ? 1 : 0;
								}
								return false;
							}
							else {
								List<GreedyConfiguration> nextCollection = collections.get(0);
								collections.remove(0);
								currentIt = nextCollection.iterator();
							}
						}
					}
				}

				@Override
				public GreedyConfiguration next() {
					if(Testbench.COLLECT_STATS)
						activeSize++;
					
					return nextItem;
				}

				@Override
				public void remove() {}
			};
		}
		
	}
	public boolean isEmpty() {
		return unstables.isEmpty() && onRhoStates.isEmpty() && symbolNeeders.isEmpty();
	}
	/**
	 * Count the number of final configurations in the greedy frontier
	 * @return
	 */
	public int getNumFinalConfigurations() {
		Set<GreedyConfiguration> goalGCs = new HashSet<>();
		
		for(GreedyConfiguration gc: unstables)
			if(!gc.isDead() && gc.isFinal()) goalGCs.add(gc);
		
		for(GreedyConfiguration gc: onRhoStates)
			if(!gc.isDead() && gc.isFinal()) goalGCs.add(gc);
		
		for(Entry<Integer, List<GreedyConfiguration>> e: symbolNeeders.entrySet())
			for(GreedyConfiguration gc: e.getValue())
				if(!gc.isDead() && gc.isFinal()) goalGCs.add(gc);
		
		return terminalGoals + goalGCs.size();
	}
	
	//Statistics
	public static void yieldStatistics(String sessionName, ResultsContainer rc) {
		rc.addSessionNumber(sessionName, "max fsize", size);
		rc.addSessionNumber(sessionName, "max snsize", maxSNSize);
		rc.addSessionNumber(sessionName, "max asize", maxActiveSize);
		rc.addSessionNumber(sessionName, "empty frontiers", emptyActiveFrontiers);
		rc.addSessionNumber(sessionName, "unstables", unstableCounter);
		rc.addSessionNumber(sessionName, "rho compatibles", rhoCompCounter);
		rc.addSessionNumber(sessionName, "symbol needers", symbolNeedCounter);
		rc.addSessionNumber(sessionName, "ignored nodes", ignoredConfigs);
		rc.addSessionNumber(sessionName, "dead nodes", deadConfigs);
		
		rhoCompCounter = 0;
		symbolNeedCounter = 0;
		unstableCounter = 0;
		maxActiveSize = 0;
		ignoredConfigs = 0;
		deadConfigs = 0;
		size = 0;
		maxSNSize = 0;
		emptyActiveFrontiers = 0;
	}
}
