package algorithms.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Just a clean way to collect test results along the way
 * @author vincent
 *
 */
public class ResultsContainer {
	//STATIC SIDE
	private static ResultsContainer container;
	
	public static ResultsContainer getContainer() {
		if(container == null) {
			container = new ResultsContainer();
		}
		
		return container;
	}
	
	//INSTANCE SIDE
	private final List<String> outputList;
	private StringBuilder sb = new StringBuilder();
	
	private ResultsContainer() {
		outputList = new ArrayList<>();
	}
	
	public void addResultString(String result) {
		outputList.add(result);
	}
	
	public void flush() {
		commit();
		
		for(String output : outputList) {
			System.out.println(output);
		}
		
		outputList.clear();
	}
	
	public void commit() {
		outputList.add(sb.toString());
		sb = new StringBuilder();
	}
	
	public void print(Object o) {
		sb.append(o.toString());
	}
	
	public void println(Object o) {
		sb.append(o.toString() + "\n");
	}

	public void clear() {
		commit();
		outputList.clear();
	}
}
