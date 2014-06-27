package testbench.programs.translator.trf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class SubClassEvaluator extends RuleEvaluator {
	private Map<String, Map<String, ReturnEvaluator>> quickSubclassMap = new HashMap<>();
	private Map<Class<?>, Map<String, ReturnEvaluator>> subclassMap = new HashMap<>();
	
	@Override
	public void addRule(String ruleLine) throws Exception {
		//Parse rule
		parseRule(ruleLine);
		
		try {
			Class<?> superClass = Class.forName(getConditionString());
			if(!subclassMap.containsKey(superClass)) {
				subclassMap.put(superClass, new HashMap<String, ReturnEvaluator>());
			}
			
			subclassMap.get(superClass).put(getMethodName(), getRe());
		} catch (ClassNotFoundException e) {
			System.out.println("    Loading rule '" + ruleLine + "':");
			System.out.println("    -> Could not load external class but added equality shortcut!");
		}
		
		if(!quickSubclassMap.containsKey(getConditionString()))
			quickSubclassMap.put(getConditionString(), new HashMap<String, ReturnEvaluator>());
		quickSubclassMap.get(getConditionString()).put(getMethodName(), getRe());
	}

	@Override
	public LinkedList<Integer> evaluate(int id, String cl, String method, String rv) throws Exception {
		if(!quickSubclassMap.containsKey(cl)) {
			boolean found = false;
			
			Class<?> subclass = Class.forName(cl);
			for(Entry<Class<?>, Map<String, ReturnEvaluator>> e : subclassMap.entrySet()) {
				if(e.getKey().isAssignableFrom(subclass)) {
					quickSubclassMap.put(cl, e.getValue());
					found = true;
				}
			}
			
			if(!found) {
				quickSubclassMap.put(cl, new HashMap<String, ReturnEvaluator>());
				System.out.println("(w) " + cl + " was not a filtered class!");
				return null;
			}
		}
		
		ReturnEvaluator rev = quickSubclassMap.get(cl).get(method);
		
		return filterEncode(id, rev == null ? null : rev.evaluate(rv));
	}
	
	public Set<String> getSubclassNames() {
		Set<String> names = new HashSet<>();
		for(Entry<String, Map<String, ReturnEvaluator>> e : quickSubclassMap.entrySet()) {
			names.add(e.getKey());
		}
		
		return names;
	}

}
