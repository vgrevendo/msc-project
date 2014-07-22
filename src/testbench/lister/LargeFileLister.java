package testbench.lister;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Scanner;

import algorithms.tools.ResultsContainer;

/**
 * Same as {@link FileWordLister} but reads a large file by parts.
 * @author vincent
 *
 */
public class LargeFileLister extends TestLister<List<Integer>> {
	private final static int SLICE_SIZE = 20_000_000;
	public static long loadingTime = 0L;
	
	private final String filename;
	private int slice = 1;
	private LargeList ll = null;
	
	public LargeFileLister(String filename) throws FileNotFoundException {
		this.filename = filename;
	}

	@Override
	protected List<Integer> nextResource() {
		try {
			return ll = new LargeList(slice++, filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
			return null;
		}
	}

	@Override
	public int size() {
		if(ll == null)
			return 100;
		if(ll.isDepleted())
			return slice-1;
		return 100;
	}
	
	@Override
	public boolean hasMore() {
		System.out.println("ll is "  + ll);
		if(ll != null)
			System.out.println(ll.isDepleted());
		return ll == null || !ll.isDepleted();
	}
	
	/**
	 * <p>Use only by iterating over the word symbol by symbol.
	 * Incurs loading overhead every 20M symbols. Overhead
	 * is recorded and can be deduced afterwards.</p>
	 * 
	 * <p>This should be done with an asynchronously loading iterator.</p>
	 * @author vincent
	 *
	 */
	public class LargeList implements List<Integer>, RandomAccess {
		//params
		private final int slices;
		
		//current state
		private int currentSlice = 0;
		private int absoluteSize = 0;
		
		private List<Integer> word = new ArrayList<>();
		
		//loading
		private final Scanner sc;
		private boolean endOfFile = false;
		
		public LargeList(int slices, String filename) throws FileNotFoundException {
			this.slices = slices;
			sc = new Scanner(new File(filename));
			System.out.print("[LFL] Initialising large trace file....");
			sc.nextLine();
			sc.nextLine();
		}

		//Unsupported
		@Override
		public boolean add(Integer arg0) {
			throw new UnsupportedOperationException();
		}
		@Override
		public void add(int arg0, Integer arg1) {
			throw new UnsupportedOperationException();
		}
		@Override
		public boolean addAll(Collection<? extends Integer> arg0) {
			throw new UnsupportedOperationException();
		}
		@Override
		public boolean addAll(int arg0, Collection<? extends Integer> arg1) {
			throw new UnsupportedOperationException();
		}
		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}
		@Override
		public boolean contains(Object arg0) {
			throw new UnsupportedOperationException();
		}
		@Override
		public boolean containsAll(Collection<?> arg0) {
			throw new UnsupportedOperationException();
		}
		@Override
		public int indexOf(Object arg0) {
			throw new UnsupportedOperationException();
		}
		@Override
		public boolean isEmpty() {
			throw new UnsupportedOperationException();
		}
		@Override
		public int lastIndexOf(Object arg0) {
			throw new UnsupportedOperationException();
		}
		@Override
		public ListIterator<Integer> listIterator() {
			throw new UnsupportedOperationException();
		}
		@Override
		public ListIterator<Integer> listIterator(int arg0) {
			throw new UnsupportedOperationException();
		}
		@Override
		public boolean remove(Object arg0) {
			throw new UnsupportedOperationException();
		}
		@Override
		public Integer remove(int arg0) {
			throw new UnsupportedOperationException();
		}
		@Override
		public boolean removeAll(Collection<?> arg0) {
			throw new UnsupportedOperationException();
		}
		@Override
		public boolean retainAll(Collection<?> arg0) {
			throw new UnsupportedOperationException();
		}
		@Override
		public Integer set(int arg0, Integer arg1) {
			throw new UnsupportedOperationException();
		}
		@Override
		public List<Integer> subList(int arg0, int arg1) {
			throw new UnsupportedOperationException();
		}
		@Override
		public Object[] toArray() {
			throw new UnsupportedOperationException();
		}
		@Override
		public <T> T[] toArray(T[] arg0) {
			throw new UnsupportedOperationException();
		}
		@Override
		public Integer get(int idx) {
			throw new UnsupportedOperationException();
		}
		@Override
		public int size() {
			return slices*SLICE_SIZE;
		}
		
		//Operations
		@Override
		public Iterator<Integer> iterator() {
			return new Iterator<Integer>() {
				int idx = SLICE_SIZE;
				boolean depleted = false;
				
				@Override
				public boolean hasNext() {
					if(idx >= word.size()) {
						if(depleted)
							return false;
						
						depleted = !loadNextSlice();
						
						if(word.size() == 0)
							return false;
						
						idx -= SLICE_SIZE;
					}
					
					return true;
				}
				@Override
				public Integer next() {
					return word.get(idx++);
				}
				@Override
				public void remove() {}
			};
		}
		private boolean loadNextSlice() {
			long loadStart = System.currentTimeMillis();
			System.out.print("[LFL]: ");
			word = new ArrayList<>(SLICE_SIZE);
			
			if(currentSlice >= slices) {
				System.out.println(" slice limit exceeded.");
				return false;
			}
			
			
			while(word.size() < SLICE_SIZE && sc.hasNextLine()) {
				String l = sc.nextLine();
				if(l.startsWith("-")) {
					endOfFile = true;
					break;
				}
				word.add(Integer.parseInt(l));
			}
			
			if(!sc.hasNextLine())
				endOfFile = true;
			
			currentSlice++;
			absoluteSize += word.size();
			
			System.out.println("found " + word.size() + " more symbols (at size " + absoluteSize + ")");
			
			loadingTime += (System.currentTimeMillis() - loadStart);
			
			return word.size() == SLICE_SIZE;
		}
		public boolean isDepleted() {
			return endOfFile;
		}
	}

	public static void yieldStatistics(String sessionName, ResultsContainer rc) {
		rc.addSessionNumber(sessionName, "loading time", (int) loadingTime);
		Map<String, List<Integer>> session = rc.getSession(sessionName);
		List<Integer> realTimesList = session.get("Time");
		int realTime = realTimesList.get(realTimesList.size()-1);
		rc.addSessionNumber(sessionName, "execution time", realTime - (int)(loadingTime));
		
		loadingTime = 0L;
	}
}
