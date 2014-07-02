package testbench.programs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import testbench.programs.tss.CleanResource;
import testbench.programs.tss.DirtyResource;
import testbench.programs.tss.Resource;

/**
 * Simply implements an instance of the Source Sink problem.
 * @author vincent
 *
 */
public class SourceSinkProgram {

	public static void main(String[] args) {
		if(args.length != 2) {
			System.out.println("Need 2 args!");
			return;
		}
		
		final int expectedNumResources = Integer.parseInt(args[0]);
		int expectedIterations = Integer.parseInt(args[1]);
		
		Random r = new Random();
		
		int numResources = 0;
		
		List<Resource> cleanResources = new ArrayList<>();
		List<Resource> dirtyResources = new ArrayList<>();
		
		//Main loop
		while(expectedIterations-- >= 0) {
			
			//Create some resources
			if(expectedNumResources > numResources) {
				final int createResources = r.nextInt(expectedNumResources);
				for(int i = 0; i < createResources; i++) {
					
					//Choose between dirty and clean
					if(r.nextInt(10)>5) {
						dirtyResources.add(new DirtyInt(r.nextInt(10)));
					} else {
						cleanResources.add(new CleanInt(r.nextInt(10)));
					}
					numResources++;
				}
			}
			
			//Sanitize some
			final int toSanitize = r.nextInt(dirtyResources.size());
			for(int i = 0; i < toSanitize; i++) {
				Resource res = dirtyResources.get(i % dirtyResources.size());
				dirtyResources.remove(res);
				res.sanitize();
				cleanResources.add(res);
			}
			
			//Combine some
			if(!dirtyResources.isEmpty()) {
				final int toCombine = r.nextInt(100);
				for(int i = 0; i < toCombine; i++) {
					//Pick 2
					Resource cleanRes = cleanResources.get(r.nextInt(cleanResources.size()));
					Resource dirtyRes = dirtyResources.get(r.nextInt(dirtyResources.size()));
					
					//Combine
					dirtyResources.add(dirtyRes.combineWith(cleanRes));
				}
			}
			
			//Sink some
			final int toSink = r.nextInt(100);
			for(int i = 0; i < toSink; i++) {
				cleanResources.get(r.nextInt(cleanResources.size())).sink();
			}
		}
		
		System.out.println("Done. Sum: " + Int.sinkValue);
		System.out.println("Sink mistakes: " + Int.sinkMistakes);
	}
	
	public static class CleanInt extends Int 
			implements CleanResource {
		public CleanInt(int val) {
			super(val, false);
		}
		
		@Override
		public Resource combineWith(Resource other) {
			if(other instanceof DirtyResource)
				return other.combineWith(this);
			
			Int i = (Int) other;
			return new CleanInt(getValue() + i.getValue());
		}
	}
	
	public static class DirtyInt extends Int
			implements DirtyResource {
		
		public DirtyInt(int val) {
			super(val, true);
		}

		@Override
		public Resource combineWith(Resource other) {
			int otherValue = ((Int)other).getValue();
			
			return new DirtyInt(getValue() + otherValue);
		}
	}
	
	public static abstract class Int implements Resource {
		private final int value;
		private boolean dirty = false;
		
		public static int sinkMistakes = 0;
		public static int sinkValue = 0;
		
		public Int(int val, boolean dirty) {
			this.value = val;
			this.dirty = dirty;
		}

		@Override
		public void sink() {
			if(dirty)
				sinkMistakes++;
			sinkValue += value;
		}
		
		@Override
		public void sanitize() {
			dirty = false;
		}
		
		protected boolean isDirty() {
			return dirty;
		}
		protected int getValue() {
			return value;
		}
		
	}

}
