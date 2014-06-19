package algorithms.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Just a clean way to collect test results along the way
 * @author vincent
 *
 */
public class ResultsContainer {
	//STATIC SIDE
	private final static ResultsContainer container = new ResultsContainer();
	
	public static ResultsContainer getContainer() {
		return container;
	}
	
	//INSTANCE SIDE
	private final List<String> outputList;
	private final Map<String, Map<String, List<Integer>>> statistics;
	private StringBuilder sb = new StringBuilder();
	
	private ResultsContainer() {
		outputList = new ArrayList<>();
		statistics = new HashMap<>();
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

	//Number sessions: store statistics intelligently
	public void createSession(String name) {
		statistics.put(name, new HashMap<String, List<Integer>>());
	}
	
	public void addSessionNumber(String sessionName, String category, Integer number) {
		Map<String, List<Integer>> session = statistics.get(sessionName);
		if(!session.containsKey(category))
			session.put(category, new ArrayList<Integer>());
		session.get(category).add(number);
	}
	
	public Map<String, List<Integer>> getSession(String name) {
		return statistics.get(name);
	}
}
