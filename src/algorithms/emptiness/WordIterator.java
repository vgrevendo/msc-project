package algorithms.emptiness;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WordIterator implements Iterable<int[]> {
	private final List<AlphabetCounter> counters;
	private int dispensed = 0;
	public final int capacity;
	
	public WordIterator(int size, Integer[] sequence) {
		counters = new ArrayList<>();
		
		int incrementFactor = 1;
		for(int c = 0; c < size; c++) {
			counters.add(new AlphabetCounter(sequence, incrementFactor));
			incrementFactor *= sequence.length;
		}
		
		capacity = (int) Math.pow(sequence.length, size);
	}

	@Override
	public Iterator<int[]> iterator() {
		return new Iterator<int[]>() {

			@Override
			public boolean hasNext() {
				return dispensed < capacity;
			}

			@Override
			public int[] next() {
				dispensed ++;
				
				int[] word = new int[counters.size()];
				
				for(int i = 0; i < counters.size(); i++) {
					word[i] = counters.get(i).next();
				}

				
				return word;
			}

			@Override
			public void remove() {
			}
		};
	}

}
