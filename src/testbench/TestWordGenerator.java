package testbench;

import java.util.Iterator;

public abstract class TestWordGenerator implements Iterable<int[]> {
	protected int wordIndex = 0;

	@Override
	public Iterator<int[]> iterator() {
		return new Iterator<int[]>() {

			@Override
			public boolean hasNext() {
				return wordIndex < size();
			}

			@Override
			public int[] next() {
				int[] w = nextWord();
				wordIndex++;
				return w;
			}

			@Override
			public void remove() {}
		};
	}
	
	protected abstract int[] nextWord();
	public abstract int size();
	
	public int getWordIndex() {
		return wordIndex-1;
	}
}
