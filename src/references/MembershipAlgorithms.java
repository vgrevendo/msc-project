package references;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import algorithms.membership.MBSDecisionAlgorithm;
import algorithms.tools.ResultsContainer;
import automata.Automaton;

public class MembershipAlgorithms {
	
	/**
	 * An algorithm using RV. For the moment, garbage collection is disabled, 
	 * and it seems that no return values are actually used...
	 */
	public static MBSDecisionAlgorithm hasNextJavaMOPReference = new MBSDecisionAlgorithm("HNP-RVMonitor") {
		
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
					if(HasNextRuntimeMonitor.hasnextEvent(iterators.get(id)))
						return true;
					break;
				case 3:
					if(HasNextRuntimeMonitor.nextEvent(iterators.get(id)))
						return true;
					break;
				}
			}
			
			return false;
		}

		@Override
		protected void yieldStatistics(String sessionName, ResultsContainer rc) {
			//References do not yield statistics
		}
	};
}
