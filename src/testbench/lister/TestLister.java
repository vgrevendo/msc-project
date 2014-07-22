package testbench.lister;

import java.util.Iterator;

/**
 * A generalised class made from the test word generator,
 * so it can generate automata as well.
 * @author vincent
 *
 * @param <T>
 */
public abstract class TestLister<T> implements Iterable<T> {
	protected int index = 0;

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {

			@Override
			public boolean hasNext() {
				return hasMore() && index < size();
			}

			@Override
			public T next() {
				T w = nextResource();
				index++;
				return w;
			}

			@Override
			public void remove() {}
		};
	}
	
	protected abstract T nextResource();
	public abstract int size();
	protected boolean hasMore() {
		return true;
	}
	
	public int getIndex() {
		return index-1;
	}
}
