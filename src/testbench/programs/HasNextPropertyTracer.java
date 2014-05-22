package testbench.programs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class HasNextPropertyTracer {
	public static final int MRI_MAX_NUM_IT = 100;
	public static final int MRI_MAX_LIST_LENGTH = 100;
	public static final int MRI_MAX_POP_FACTOR = 20;
	
	public static void main(String[] args) {
		multipleRandomIterators();
		
		System.out.println("DONE");
	}
	
	public static void singleIteratorLoop() {
		List<Integer> list = new ArrayList<>();
		
		for(int i = 0; i < 100; i++)
			list.add(i);
		
		int s = 0;
		
		Iterator<Integer> it = list.iterator();
		while(it.hasNext()) {
			s += it.next();
		}
	}
	
	/**
	 * A more sophisticated example, where it could be unclear that 
	 * hasNext is actually called before next.
	 */
	public static void multipleRandomIterators() {
		int sum = 0;
		
		//Populate a random list
		int length = (int) (Math.random() * MRI_MAX_LIST_LENGTH)+1; 
		ArrayList<Integer> list = new ArrayList<>(length);
		
		for(int i = 0; i < length; i++) {
			list.add(i);
		}
		
		//Create all iterators
		int numIterators = (int) (Math.random() * MRI_MAX_NUM_IT)+1;
		List<Iterator<Integer>> iterators = new ArrayList<>(numIterators);
		
		for(int i = 0; i < numIterators; i++) 
			iterators.add(list.iterator());
		
		//Iterate randomly with all the iterators
		Iterator<Integer> last = iterators.get(0);
		while(true) {
			if(!last.hasNext()) {
				iterators.remove(last);
				if(iterators.isEmpty())
					break;
				else {
					last = iterators.get(0);
					continue;
				}
			}
			
			//Last has at least one element left.
			//Choose a few iterators from the list, check what
			// they have left, prune if necessary and
			// remember those who answer positively
			int numChosen = (int) (Math.random() * MRI_MAX_POP_FACTOR)+1;
			Set<Iterator<Integer>> toPop = new HashSet<>(numChosen);
			
			for(int i = 0; i < numChosen; i++) {
				int idx = (int) (Math.random() * iterators.size());
				Iterator<Integer> candidate = iterators.get(idx);
				if(candidate.hasNext())
					toPop.add(candidate);
				else
					iterators.remove(candidate);
			}
			
			//Invoke next on those that were chosen (unique nexts)
			for(Iterator<Integer> it: toPop) {
				sum += it.next();
			}
		}
		
		System.out.println(numIterators + " iterators processed over " + length + " elements");
		System.out.println("Expected sum: " + (numIterators*length*(length-1)/2));
		System.out.println("Real sum:     " + sum);
	}

}
