package testbench.programs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HasNextPropertyTracer {
	public static void main(String[] args) {
		List<Integer> list = new ArrayList<>();
		
		for(int i = 0; i < 100; i++)
			list.add(i);
		
		int s = 0;
		
		Iterator<Integer> it = list.iterator();
		while(it.hasNext()) {
			s += it.next();
		}
		
		System.out.println("DONE");
	}

}
