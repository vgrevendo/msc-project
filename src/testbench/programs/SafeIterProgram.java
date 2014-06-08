package testbench.programs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SafeIterProgram {
	public static final int NUM_COLLECTIONS = 100;
	public static final int NUM_ITERATORS = 50;
	public static final int NUM_ELEMENTS = 100;

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
		
		//Instantiate iterators
		List<Iterator<Integer>> iterators = new ArrayList<>();
		
		for(Collection<Integer> col : collections) {
			int numIterators = (int) (Math.random()*NUM_ITERATORS);
			for(int i = 0; i < numIterators; i++) {
				iterators.add(col.iterator());
			}
		}
		
		//Call next on iterators
		int sum = 0;
		
		while(!iterators.isEmpty()) {
			int itIdx = (int) (Math.random() * iterators.size());
			Iterator<Integer> it = iterators.get(itIdx);
			
			if(it.hasNext())
				sum += it.next();
			else
				iterators.remove(itIdx);
		}
		
		System.out.println("All done. Sum: " + sum);
	}

}
