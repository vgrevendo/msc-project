package algorithms.emptiness;

public class AlphabetCounter {
	private final Integer[] sequence;
	private final int incrementFactor;
	private int counter = 0;
	private int cursor = 0;
	
	public AlphabetCounter(Integer[] sequence, int incrementFactor) {
		this.sequence = sequence;
		this.incrementFactor = incrementFactor;
	}
	
	public int next() {
		int current = sequence[cursor];
		
		counter++;
		if(counter % incrementFactor == 0)
			cursor = (cursor + 1) % sequence.length;
		
		return current;
	}
}
