package testbench.programs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SafeIterProgram {
	public static final int NUM_COLLECTIONS = 1000;
	public static final int NUM_ITERATORS = 500;
	public static final int NUM_ELEMENTS = 100;
	public static final double PROB_UPD_COL = 0.05;

	public static void main(String[] args) {
		//Instantiate collections
		List<Collection<Integer>> collections = new ArrayList<>();
		
		for(int c = 0; c < NUM_COLLECTIONS; c++) {
			int numElements = (int) (Math.random() * NUM_ELEMENTS);
			List<Integer> col = new ArrayList<Integer>(numElements);
			
			for(int e = 0; e < numElements; e++)
				col.add(e);
			
			collections.add(col);
		}
		
		Map<Collection<Integer>, List<Iterator<Integer>>> iterators = new HashMap<>();
		for(Collection<Integer> col : collections) {
			iterators.put(col, new ArrayList<Iterator<Integer>>());
		}
		
		
		while(true) {
			//Instantiate iterators
			for(Collection<Integer> col : collections) {
				int numIterators = (int) (Math.random()*NUM_ITERATORS);
				for(int i = 0; i < numIterators; i++) {
					iterators.get(col).add(col.iterator());
				}
			}
			
			for(Entry<Collection<Integer>, List<Iterator<Integer>>> e : iterators.entrySet()) {
				List<Iterator<Integer>> its = e.getValue();
				
				while(!its.isEmpty()) {
					if(Math.random() < PROB_UPD_COL) {
						e.getKey().add(52);
						its.clear();
						break;
					}
					
					int itIdx = (int) (Math.random() * its.size());
					Iterator<Integer> it = its.get(itIdx);
					
					if(it.hasNext())
						it.next();
					else
						its.remove(itIdx);
				}
			
				
			}
			
		}
		
	}

}
