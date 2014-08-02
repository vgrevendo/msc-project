package references;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import testbench.lister.LargeFileLister;
import algorithms.membership.MBSDecisionAlgorithm;
import algorithms.tools.ResultsContainer;
import automata.Automaton;

public class MembershipAlgorithms {
	
	/**
	 * An algorithm using RV. For the moment, garbage collection is disabled, 
	 * and it seems that no return values are actually used...
	 */
	public static MBSDecisionAlgorithm hasNextSimpleRef = new MBSDecisionAlgorithm("HNP-RVMonitor") {
		
		@Override
		public boolean decide(Automaton automaton, List<Integer> word) {
			Map<Integer, Iterator<Integer>> iterators = new HashMap<>();
			ArrayList<Integer> collection = new ArrayList<>();
			collection.add(1);
			
			Iterator<Integer> wordIt = word.iterator();
			while(wordIt.hasNext()) {
				Integer id = wordIt.next();
				if(!wordIt.hasNext())
					break;
				int method = wordIt.next();
				if(!wordIt.hasNext())
					break;
				wordIt.next(); //ignore return values
				
				switch(method) {
				case 1:
					iterators.put(id, collection.iterator());
					break;
				case 2:
					if(references.hasnextsimple.HasNextRuntimeMonitor.hasnextEvent(iterators.get(id)))
						return true;
					break;
				case 3:
					if(references.hasnextsimple.HasNextRuntimeMonitor.nextEvent(iterators.get(id)))
						return true;
					break;
				}
			}
			
			return false;
		}

		@Override
		protected void yieldStatistics(String sessionName, ResultsContainer rc) {
			//No statistics for the moment
		}
	};
	
	/**
	 * An algorithm using RV. For the moment, garbage collection is disabled, 
	 * but return values of hasNext are considered.
	 */
	public static MBSDecisionAlgorithm hasNextTrueRef = new MBSDecisionAlgorithm("HNP-True-RVMonitor") {
		
		@Override
		public boolean decide(Automaton automaton, List<Integer> word) {
			Map<Integer, Iterator<Integer>> iterators = new HashMap<>();
			ArrayList<Integer> collection = new ArrayList<>();
			collection.add(1);
			
			Iterator<Integer> wordIt = word.iterator();
			while(wordIt.hasNext()) {
				Integer id = wordIt.next();
				if(!wordIt.hasNext())
					break;
				int method = wordIt.next();
				
				//Check if we need a return value later
				switch(method) {
				case 0: //Iterator.<init>
					iterators.put(id, collection.iterator());
					break;
				case 1: //Iterator.hasNext
					//Check return value here
					if(!wordIt.hasNext())
						break;
					int rv = wordIt.next();
					
					if(rv == 4) {
						references.hasnexttrue.HasNextRuntimeMonitor.hasnexttrueEvent(iterators.get(id), true);
					} else if(rv == 5) {
						references.hasnexttrue.HasNextRuntimeMonitor.hasnextfalseEvent(iterators.get(id), false);
					} else {
						System.out.println("Unknown return value for hasNext: " + rv);
						break;
					}
					
					break;
				case 2: //Iterator.next
					references.hasnexttrue.HasNextRuntimeMonitor.nextEvent(iterators.get(id));
					break;
				case 3: //Iterator.finalize
					iterators.remove(id);
					break;
				}
			}
			
			return references.hasnexttrue.HasNextRuntimeMonitor.isMember();
		}

		@Override
		protected void yieldStatistics(String sessionName, ResultsContainer rc) {
			//Necessary if large files are used!
			LargeFileLister.yieldStatistics(sessionName, rc);
		}
	};
}
