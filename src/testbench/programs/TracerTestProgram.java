package testbench.programs;

import java.util.ArrayList;
import java.util.Iterator;

public class TracerTestProgram {

	public static void main(String[] args) {
		ArrayList<Integer> list = new ArrayList<>();
		list.add(0);
		list.add(1);
		list.add(2);
		
		Iterator<Integer> it = list.iterator();
		while(it.hasNext()) 
			System.out.println(it.next());
		
		System.out.println("DONE");
	}

}
