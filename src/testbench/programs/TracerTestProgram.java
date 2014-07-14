package testbench.programs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TracerTestProgram {

	public static void main(String[] args) {
		Set<Integer> list = new HashSet<>();
		list.add(0);
		list.add(1);
		list.add(2);
		
		Iterator<Integer> it = list.iterator();
		while(it.hasNext()) 
			System.out.println(it.next());
		
		System.out.println("DONE");
	}

}
